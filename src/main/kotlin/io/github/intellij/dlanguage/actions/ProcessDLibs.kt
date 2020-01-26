package io.github.intellij.dlanguage.actions

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.impl.OrderEntryUtil
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBList
import com.intellij.util.Consumer
import io.github.intellij.dlanguage.icons.DlangIcons
import io.github.intellij.dlanguage.library.DlangLibraryType
import io.github.intellij.dlanguage.module.DlangModuleType
import io.github.intellij.dlanguage.project.DubConfigurationParser
import io.github.intellij.dlanguage.project.DubPackage
import io.github.intellij.dlanguage.settings.ToolKey
import io.github.intellij.dlanguage.utils.DToolsNotificationListener
import java.util.*

/**
 * ported from Java on 06/02/18
 *
 * todo: this class needs a rethink. It's currently tied to the user having dub configured. Also, the Action shouldn't
 * need to be called manually. For dub projects we should scan dub.json/sdl for changes or when opening/importing
 * a project (The same way that the Maven/Gradle integration works) and when not using dub we should just
 * detect when the user adds/removes a library.
 */
class ProcessDLibs : AnAction("Process D Libraries", "Processes the D Libraries", DlangIcons.SDK), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project == null || project.isDisposed) {
            displayError(e, "Unable to process D libraries - No active project.")
            return
        }
        val modules = DlangModuleType.findModules(project)
        val size = modules.size
        when (size) {
            0 -> displayError(e, "Unable to process D libraries - No DLanguage modules are used in this project.")
            1 -> processDLibs(project, modules.iterator().next())
            else -> showModuleChoicePopup(e, project, modules)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = enabled(e)
    }

    companion object {
        private val LOG = Logger.getInstance(ProcessDLibs::class.java)
        private val MENU_PATH = "Tools > Process D Libraries"
        private val NOTIFICATION_GROUPID = "Process D Libs"
        private var dubPathAlreadWarned = false

        /**
         * This method is used from various places, when re-working the way libs are processed this should be marked
         * as deprecated and deleted in a future release
         */
        @JvmStatic
        fun processDLibs(project: Project, module: Module, mostlySilentMode: Boolean, buildBefore: Boolean) {
            ApplicationManager.getApplication().invokeAndWait({
                val task = object : Task.Backgroundable(project,
                    "Updating Dub Libraries") {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.text = "processing libs for ${module.name}"
                        indicator.isIndeterminate = true
                        processDLibsImpl(project, module, mostlySilentMode, buildBefore)
                    }
                }
                ProgressManager.getInstance()
                    .runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
            }, ModalityState.defaultModalityState())
        }

        // Worth seeing if this is optimal. Refer to documentation here:
        // http://www.jetbrains.org/intellij/sdk/docs/reference_guide/project_model/library.html
        private fun processDLibsImpl(project: Project, module: Module,
                                     mostlySilentMode: Boolean, buildBefore: Boolean) {
            //todo needs build/fetch before adding libs, also needs to keep track of libs

            // remove all existing libs
            ModuleRootModificationUtil.updateModel(module) { model ->
                model.orderEntries
                        .filterIsInstance<LibraryOrderEntry>()
                        .forEach {
                            it.library?.let { lib ->
                                model.moduleLibraryTable.removeLibrary(lib)
                            }
                            model.removeOrderEntry(it)
                        }
            }

            // this should not be necessary since intellij is supposed to filter out duplicate libraries.
            // see #451 #538
            LibraryTablesRegistrar.getInstance()
                .getLibraryTable(project)
                .libraries
                .forEach { lib ->
                    ApplicationManager.getApplication().invokeAndWait {
                        //removeLibraryIfNeeded(module, lib)

                        val model = ModifiableModelsProvider.SERVICE.getInstance()
                            .getLibraryTableModifiableModel(module.project)

                        model.removeLibrary(lib)
                        //.commit()
                    }
                }

            // ask dub for required libs
            val dubPath = ToolKey.DUB_KEY.path

            //final String groupId = e.getPresentation().getText();
            if (dubPath == null) {
                if (!this.dubPathAlreadWarned) {
                    Notifications.Bus.notify(
                        Notification(NOTIFICATION_GROUPID, "Process D Libraries",
                            "DUB executable path is empty<br/><a href='configureDLanguageTools'>Configure</a>",
                            NotificationType.WARNING, DToolsNotificationListener(project)),
                        project)
                    this.dubPathAlreadWarned = true
                }
                return
            }

            val dubParser = DubConfigurationParser(project, dubPath,
                mostlySilentMode)
            if (dubParser.canUseDub()) {
                val dependencies = dubParser.dubPackageDependencies
                for (pkg in dependencies) {
                    val fullName = pkg.name + "-" + pkg.version
                    createLibraryDependency(module, project, fullName, pkg)
                }
            } else {
                LOG.info("not possible to run 'dub describe'")
            }

            if (!mostlySilentMode) {
                Notifications.Bus.notify(
                    Notification(NOTIFICATION_GROUPID, "Process D Libraries",
                        "Added your dub dependency libraries",
                        NotificationType.INFORMATION, DToolsNotificationListener(project)),
                    project)
            }
        }

        private fun enabled(e: AnActionEvent): Boolean {
            val project = AnAction.getEventProject(e) ?: return false
            val dubPath = ToolKey.DUB_KEY.path ?: return false
            return dubPath.isNotEmpty() && DlangModuleType.findModules(project).isNotEmpty()
        }

        private fun showModuleChoicePopup(e: AnActionEvent, project: Project, modules: Collection<Module>) {
            val list = JBList(JBList.createDefaultListModel<Module>(*modules.toTypedArray()))
            val popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle("Process D libraries for module")
                .setItemChoosenCallback { processDLibs(AnAction.getEventProject(e), list.selectedValue as Module) }
                .createPopup()
            popup.showCenteredInCurrentWindow(project)
        }

        private fun processDLibs(project: Project?, module: Module) {
            if (project == null || project.isDisposed) {
                LOG.warn("Unable to process D libraries - No active D project.")
                //displayError(project, "Unable to process D libraries - No active D project.");
                return
            }
            processDLibs(project, module, false, false)
        }

        private fun createLibraryDependency(module: Module, project: Project,
                                            libraryName: String, dubPackage: DubPackage) {
            val sources = getSourcesVirtualFile(dubPackage)
            val projectLibraryTable = LibraryTablesRegistrar.getInstance()
                .getLibraryTable(project)
            val projectLibraryModel = projectLibraryTable
                .modifiableModel

            val library = projectLibraryModel.createLibrary(libraryName, DlangLibraryType.DLANG_LIBRARY)
            val libraryModel = library.modifiableModel

            if (sources != null) {
                libraryModel.addRoot(sources, OrderRootType.CLASSES)
                //todo add binary libs/dub.json as well
                ApplicationManager.getApplication()
                    .invokeAndWait {
                        ApplicationManager.getApplication().runWriteAction {
                            libraryModel.commit()
                            projectLibraryModel.commit()
                            for (projectModule in ModuleManager.getInstance(project)
                                .modules) {
                                ModuleRootModificationUtil.addDependency(projectModule, library)
                            }

                        }
                    }

            }
        }

        // todo: This method needs breaking down. Also, a dub package can have multiple source directories
        private fun getSourcesVirtualFile(dubPackage: DubPackage): VirtualFile? {
            // for now I'm putting this hideous kludge in which makes it work they way it always has
            val srcDir = if(dubPackage.sourcesDirs.isNotEmpty()) {
                if(dubPackage.sourcesDirs.size > 1) {
                    LOG.warn("it's likely that we're not processing ${dubPackage.name} correctly. ${dubPackage.sourcesDirs.size} source folders found")
                }
                dubPackage.sourcesDirs[0]
            } else "source"

            val sourcesPathUrl = if (SystemInfo.isWindows) {
                VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, dubPackage.path + srcDir.replace("/", "\\"))
            } else {
                VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, dubPackage.path + srcDir)
            }

            val sources = VirtualFileManager.getInstance().refreshAndFindFileByUrl(sourcesPathUrl)

            if (sources == null) {
                LOG.info("sources not found, fetching them")
                val commandLine = GeneralCommandLine()
                val dubBinaryPath = ToolKey.DUB_KEY.path
                if (dubBinaryPath == null) {
                    LOG.error("dub executable should be configured")
                    return null
                }
                commandLine.exePath = dubBinaryPath
                val parametersList = commandLine.parametersList
                parametersList.addParametersString("fetch")
                parametersList.addParametersString(dubPackage.name)
                parametersList.addParametersString("--version=" + dubPackage.version)

                val dubCommand = commandLine.commandLineString
                val process: OSProcessHandler
                try {
                    process = OSProcessHandler(commandLine.createProcess(), dubCommand)
                } catch (e: ExecutionException) {
                    LOG.error(e)
                    return null
                }

                val builder = StringBuilder()
                val errors = ArrayList<String>()

                process.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        when (outputType.toString()) {
                            "stdout" -> builder.append(event.text)
                            "stderr" -> errors.add(event.text)
                        }
                    }
                })

                process.startNotify()
                process.waitFor()

                LOG.debug("output from fetching package:" + builder.toString())

                for (error in errors) {
                    LOG.error("error while fetching:" + error)
                }

                val exitCode = process.exitCode
                if (exitCode != null && exitCode == 0) {
                    //try loading again
                    return VirtualFileManager.getInstance().refreshAndFindFileByUrl(sourcesPathUrl)
                } else {
                    LOG.error("exitcode was no zero for fetching package")
                }
            }
            return sources
        }

        private fun removeLibraryIfNeeded(module: Module, lib: Library) {
            ApplicationManager.getApplication().assertIsDispatchThread()

            val modelsProvider = ModifiableModelsProvider.SERVICE.getInstance()
            val model = modelsProvider.getModuleModifiableModel(module)
            //val dLibraryEntry = OrderEntryUtil.findLibraryOrderEntry(model, lib.name)
            val dLibraryEntry = OrderEntryUtil.findLibraryOrderEntry(model, lib)

            if (dLibraryEntry != null) {
                ApplicationManager.getApplication().runWriteAction {
                    val library = dLibraryEntry.library
                    if (library != null) {
                        val table = library.table
                        if (table != null) {
                            table.removeLibrary(library)
                            model.removeOrderEntry(dLibraryEntry)
                            modelsProvider.commitModuleModifiableModel(model)
                        }
                    } else {
                        modelsProvider.disposeModuleModifiableModel(model)
                    }
                }
            } else {
                ApplicationManager.getApplication()
                    .runWriteAction { modelsProvider.disposeModuleModifiableModel(model) }
            }
        }

        private fun displayError(e: AnActionEvent, message: String) {
            displayError(AnAction.getEventProject(e), message)
        }

        private fun displayError(project: Project?, message: String) {
            //final String groupId = e.getPresentation().getText();
            Notifications.Bus.notify(
                Notification(NOTIFICATION_GROUPID, "Process D libs", message, NotificationType.ERROR),
                project
            )
            LOG.warn(message)
        }
    }

}
