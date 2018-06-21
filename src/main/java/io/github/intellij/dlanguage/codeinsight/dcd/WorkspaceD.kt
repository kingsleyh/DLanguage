package io.github.intellij.dlanguage.codeinsight.dcd

import com.intellij.execution.ExecutionException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vcs.changes.shelf.OpenShelfSettings
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import io.github.intellij.dlanguage.settings.ToolSettings
import io.github.intellij.dlanguage.DlangSdkType
import io.github.intellij.dlanguage.settings.SettingsChangeNotifier
import io.github.intellij.dlanguage.settings.ToolKey
import io.github.intellij.dlanguage.utils.ExecUtil
import io.github.intellij.dlanguage.utils.NotificationUtil

import java.io.*
import java.util.ArrayList
import java.util.concurrent.Executors

import io.github.intellij.dlanguage.utils.DUtil.isNotNullOrEmpty
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Process wrapper for Workspace-d.  Implements ModuleComponent so destruction of processes coincides with closing projects.
 */
class WorkspaceD
/**
 * Private constructor used during module component initialization.
 */
(val module: Module) : ModuleComponent, SettingsChangeNotifier
{

    private val workingDirectory: String

    // if no settings for the path set, then check in the Path
    private var workspaceDPath: String = "workspace-d"
    private var dcdClientPath: String = "dcd-client"
    private var dcdServerPath: String = "dcd-server"

    private var input: DataInputStream? = null
    private var err: DataInputStream? = null
    private var out: DataOutputStream? = null
    private var requestNum: Int = 0
    private var process: Process? = null

    private val executorService = Executors.newSingleThreadExecutor()

    private val rootSourceDir: String?
        get()
        {
            val myProject = module.project
            val sourceRoots = ArrayList<VirtualFile>()
            val projectRootManager = ProjectRootManager.getInstance(myProject)
            ContainerUtil.addAll<VirtualFile, VirtualFile, List<VirtualFile>>(sourceRoots, *projectRootManager.contentSourceRoots)
            return if (sourceRoots.isEmpty()) null else sourceRoots[0].path
        }

    private val compilerSourceDirs: List<String>
        get()
        {
            val compilerSources = ArrayList<String>()
            val moduleRootManager = ModuleRootManager.getInstance(module)
            val sdk = moduleRootManager.sdk

            if (sdk != null && sdk.sdkType is DlangSdkType)
            {
                val path = sdk.homePath

                if (isNotNullOrEmpty(path))
                {
                    val root = path!!.replace("/bin", "") + "/src"
                    compilerSources.add("$root/phobos")
                    compilerSources.add("$root/druntime/import")
                }
            }
            return compilerSources
        }

    init
    {

        workspaceDPath = ToolKey.WORKSPACE_D_KEY.path ?: "workspace-d"
        dcdClientPath = ToolKey.DCD_CLIENT_KEY.path ?: "dcd-client"
        dcdServerPath = ToolKey.DCD_SERVER_KEY.path ?: "dcd-server"

        this.workingDirectory = lookupWorkingDirectory()
        // Ensure that we are notified of changes to the settings.
        module.project.messageBus.connect().subscribe(SettingsChangeNotifier.DCD_TOPIC, this)
    }


    internal fun exec()
    {
        if (process == null || !process!!.isAlive)
        {
            println("Process isn't available anymore, respawn it")
            kill()
            spawnProcess()
        }
    }

    private fun spawnProcess()
    {

        LOG.debug("Spawning Workspace-d process at: $workspaceDPath")

        try
        {

            val processBuilder = ProcessBuilder(workspaceDPath)

            process = processBuilder.start()
            process?.let {
                err = DataInputStream(it.errorStream)
                input = DataInputStream(it.inputStream)
                out = DataOutputStream(it.outputStream)
            }
            LOG.info("Workspace-d process started")


            println("Started Workspace-d root: $workingDirectory source: $rootSourceDir")


            val escapedWD = SimpleJSON.quote(workingDirectory)
            setupDub(escapedWD)
            setupDcd(escapedWD)
            startDcd()
            startServer()
            refreshImports()
        } catch (e: ExecutionException)
        {
            Notification.fire(
                Notification("Workspace-D", "Unable to start workspace-d process", "Please check workspace-d path", NotificationType.ERROR),
                OpenShelfSettings())
            LOG.error("Error spawning DCD process", e)
        }

    }

    fun setupDub(projectRoot: String)
    {
        val response = sendCommand("""
{
	"cmd": "load",
	"components": ["dub"],
	"dir": $projectRoot
}""")

        println("Setup Dub: $response")
    }

    fun setupDcd(projectRoot: String)
    {
        val dcdClient = SimpleJSON.quote(dcdClientPath)
        val dcdServer = SimpleJSON.quote(dcdServerPath)

        val response = sendCommand("""
{
	"cmd": "load",
	"components": ["dcd"],
	"dir": $projectRoot,
    "autoStart": false,
    "clientPath": $dcdClient,
    "serverPath": $dcdServer
}""")
        println("Setup DCD: $response")
    }

    fun startDcd()
    {
        val response = sendCommand("""
{
	"cmd": "dcd",
	"subcmd": "find-and-select-port",
    "port": 9166
}""")
        println("Start DCD: $response")
    }

    fun startServer()
    {
        val imports = compilerSourceDirs.joinToString { SimpleJSON.quote(it) }

        val response = sendCommand("""
{
	"cmd": "dcd",
	"subcmd": "start-server",
    "additionalImports": [ $imports ]
}""")
        println("Start Server: $response")
    }

    fun refreshImports()
    {
        val response = sendCommand("""
{
	"cmd": "dcd",
	"subcmd": "refresh-imports",
    "port": 9166
}""")
        println("Refresh Imports: $response")
    }


    fun listCompletion(code: String, pos: Int): String
    {
        val response = sendCommand("""
          {"cmd": "dcd","subcmd": "list-completion", "code": ${SimpleJSON.quote(code)}, "pos": $pos, "full": true}
          """)
        return response
    }

    private fun sendCommand(json: String): String
    {
        out?.let {
            requestNum += 1

            val data = json.toByteArray(charset("UTF-8"))
            val lengthBuf = ByteBuffer.allocate(4).putInt(data.size + 4).array()
            val idBuf = ByteBuffer.allocate(4).putInt(requestNum).array()

            it.write(lengthBuf)
            it.write(idBuf)
            it.write(data)
            it.flush()
        }
        return response()
    }

    private fun response(): String
    {
        input?.let {
            val l = it.readInt()
            val buff = ByteArray(l)
            it.read(buff, 0, l)
            return String(buff, Charset.forName("UTF-8")).trim { it <= ' ' }
        }
        return ""
    }

    private fun lookupWorkingDirectory(): String
    {
        return ExecUtil.guessWorkDir(module)
    }

    /**
     * Kills the existing process.
     */
    @Synchronized
    private fun kill()
    {
        process?.destroyForcibly()
        err?.close()
        input?.close()
        out?.close()

        process = null
        err = null
        input = null
        out = null
    }

    // Implemented methods for SettingsChangeNotifier
    override fun onSettingsChanged(settings: ToolSettings)
    {
        LOG.debug("Workspace-d settings changed")
        kill()

        workspaceDPath = ToolKey.WORKSPACE_D_KEY.path ?: "workspace-d"
        dcdClientPath = ToolKey.DCD_CLIENT_KEY.path ?: "dcd-client"
        dcdServerPath = ToolKey.DCD_SERVER_KEY.path ?: "dcd-server"

        restart()
    }

    /**
     * Restarts workspace-d.
     */
    @Synchronized
    fun restart()
    {
        kill()
        spawnProcess()
    }

    private fun displayError(message: String)
    {
        displayError(module.project, message)
    }

    override fun moduleAdded()
    {
        // No need to do anything here.
    }

    override fun initComponent()
    {
        println("Init WorkspaceD")
        spawnProcess()
    }

    // Implemented methods for ModuleComponent.

    override fun disposeComponent()
    {
        executorService.shutdownNow()
        kill()
    }

    override fun getComponentName(): String
    {
        return "WorkspaceD"
    }

    companion object
    {

        private val LOG = Logger.getInstance(WorkspaceD::class.java)

        private fun displayError(project: Project, message: String)
        {
            NotificationUtil.displayToolsNotification(NotificationType.ERROR, project, "dcd error", message)
        }
    }
}


class SimpleJSON
{

    companion object
    {
        fun quote(string: String?): String
        {
            if (string == null || string.isEmpty())
            {
                return "\"\""
            }

            var c: Char = 0.toChar()
            var i: Int
            val len = string.length
            val sb = StringBuilder(len + 4)
            var t: String

            sb.append('"')
            i = 0
            while (i < len)
            {
                c = string[i]
                when (c)
                {
                    '\\', '"' ->
                    {
                        sb.append('\\')
                        sb.append(c)
                    }
                    '/' ->
                    {
                        //                if (b == '<') {
                        sb.append('\\')
                        //                }
                        sb.append(c)
                    }
                    '\b' -> sb.append("\\b")
                    '\t' -> sb.append("\\t")
                    '\n' -> sb.append("\\n")
                //'\f' -> sb.append("\\f")
                    '\r' -> sb.append("\\r")
                    else -> if (c < ' ')
                    {
                        t = "000" + Integer.toHexString(c.toInt())
                        sb.append("\\u" + t.substring(t.length - 4))
                    } else
                    {
                        sb.append(c)
                    }
                }
                i += 1
            }
            sb.append('"')
            return sb.toString()
        }
    }
}
