package `in`.hridayan.ashell.core.shizuku.service

import android.content.Context
import android.system.Os
import androidx.annotation.Keep
import ashell.core.shizuku.IShellProcess
import ashell.core.shizuku.IShellUserService
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.system.exitProcess

private const val ENV_SEPARATOR = '='
private const val EXIT_CODE_DESTROYED = 0

/**
 * Shizuku UserService running as the Shizuku server identity (shell or root).
 * Started by Shizuku through `app_process`; this is not an Android application process, so no
 * dependency injection or framework services are used here.
 */
class ShellUserService() : IShellUserService.Stub() {

    private val liveProcesses = CopyOnWriteArraySet<ShellProcessBinder>()

    @Keep
    @Suppress("unused")
    constructor(context: Context) : this()

    override fun newProcess(cmd: Array<String>, env: Array<String>?, dir: String?): IShellProcess {
        val process = startProcess(cmd, env, dir)
        val binder = ShellProcessBinder(process) { liveProcesses.remove(it) }
        liveProcesses.add(binder)
        return binder
    }

    override fun getUid(): Int = Os.getuid()

    override fun destroy() {
        liveProcesses.forEach { it.destroy() }
        liveProcesses.clear()
        exitProcess(EXIT_CODE_DESTROYED)
    }

    private fun startProcess(cmd: Array<String>, env: Array<String>?, dir: String?): Process {
        val builder = ProcessBuilder(cmd.toList())
        env?.let { applyEnvironment(builder, it) }
        dir?.let { builder.directory(File(it)) }
        return try {
            builder.start()
        } catch (e: IOException) {
            throw IllegalStateException("Could not start ${cmd.firstOrNull()}: ${e.message}", e)
        }
    }

    private fun applyEnvironment(builder: ProcessBuilder, env: Array<String>) {
        val environment = builder.environment()
        environment.clear()
        env.forEach { entry ->
            val separator = entry.indexOf(ENV_SEPARATOR)
            if (separator > 0) {
                environment[entry.substring(0, separator)] = entry.substring(separator + 1)
            }
        }
    }
}
