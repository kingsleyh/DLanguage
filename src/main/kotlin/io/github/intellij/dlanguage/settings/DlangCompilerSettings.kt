package io.github.intellij.dlanguage.settings

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.SdkComboBox
import com.intellij.openapi.roots.ui.configuration.SdkComboBoxModel
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel
import io.github.intellij.dlanguage.DlangSdkType
import java.util.function.Predicate

// todo: decide if I should maintain a list of SDK objects or a list of Strings (for the paths) and other related stuff
data class DlangCompilerSettings(
    var sdks: List<Sdk>,
    var sdk: Sdk? = null
)

/**
 * This class is setup in clion-only.xml
 */
class DlangCompilerConfigurable : BoundSearchableConfigurable("D Compiler", "DlangCompilerConfigurable"), Disposable {

    private val log = Logger.getInstance(javaClass)

    private val project = ProjectManager.getInstance().defaultProject
//    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()
//    private val dlangCompilerSettings: DlangCompilerSettings

    private val sdkComboBox = SdkComboBox(
        SdkComboBoxModel.createSdkComboBoxModel(
            project,
            ProjectSdksModel(), // cannot use ProjectStructureConfigurable as it's not in CLion, so cannot do ProjectStructureConfigurable.getInstance(ProjectManager.getInstance().defaultProject).projectJdksModel,
            Predicate { sdkTypeId -> DlangSdkType.SDK_TYPE_ID == sdkTypeId.name } //Condition { sdk: SdkTypeId -> sdk is DlangSdkType },
            //null,
            //null
        )
    )

    private val phobosTextField = JBTextField()
    private val druntimeTextField = JBTextField()


    // is initialised whenever settings window is opened
    init {
//        val dlangCompilerState: DlangCompilerState = ServiceManager.getService(DlangCompilerState::class.java)
//        dlangCompilerSettings = dlangCompilerState.settings

        sdkComboBox.addActionListener { event ->
            (event.source as SdkComboBox).getSelectedSdk()?.let { sdk ->
                // todo: consider adding root types for PHOBOS and DRUNTIME so don't need to filter OrderRootType.SOURCES based on path
                sdk.rootProvider.getFiles(OrderRootType.SOURCES).iterator().forEach { root ->
                    if(root.isValid && root.path.contains("phobos")) {
                        phobosTextField.text = root.canonicalPath
                    } else if(root.isValid && root.path.contains("druntime")) {
                        druntimeTextField.text = root.canonicalPath
                    }
                }
            }
        }
    }

    override fun isModified(): Boolean {
        return sdkComboBox.getSelectedSdk() != null
    }

    /**
     * User has clicked apply button. save state here
     */
    override fun apply() {
        sdkComboBox.getSelectedSdk()?.let {
            log.info("Need to save " + it.versionString)

            WriteAction.run<Exception> {
                ProjectRootManager.getInstance(project).projectSdk = it
                //ModuleRootModificationUtil.setModuleSdk()
            }
        }
    }

    // need to actually create the UI and register any listeners here
    // for example code see: DlangProjectGenerator and DubBinaryForModuleStep
    // BoundSearchableConfigurable has createPanel()
    override fun createPanel(): DialogPanel {

        return panel {
            titledRow("D Compiler") {
                row("DMD") {
                    sdkComboBox(comment = "Various D compilers can be downloaded from <a href=\"https://dlang.org\">dlang.org</a>.<br/>We currently support DMD (the reference compiler) only")
//                        .withBinding(
//                            componentGet = { it.getSelectedSdk() },
//                            componentSet = { cb, sdk -> sdk?.let { cb.setSelectedSdk(it) } },
//                            modelBinding = PropertyBinding({ dlangCompilerSettings.sdk }, { dlangCompilerSettings.sdk = it })
//                        )
                        .focused()
                    link("Download") {
                        BrowserUtil.browse("https://dlang.org/download.html")
                    }
                }
            }

            titledRow("Import Paths") {
                row("Phobos") {
                    phobosTextField(comment = "The standard library that ships with a D compiler").enabled(false)
                }
                row("DRuntime") {
                    druntimeTextField(comment = "Runtime library for the D programming language").enabled(false)
                }
                commentRow("""When selecting a D compiler, the paths for <a href="https://dlang.org/phobos/">phobos</a> and druntime should be auto-detected.<br/>For features such as code-completion (via DCD) to work, these paths must be set.""".trimMargin())
            }
        }
    }

    override fun dispose() {
        // todo: clean up
    }
}
