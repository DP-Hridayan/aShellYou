package `in`.hridayan.ashell.core.shizuku.service

import android.os.ParcelFileDescriptor
import ashell.core.shizuku.IShellProcess
import java.io.IOException
import java.io.InputStream

/**
 * Exposes a child [Process] running in the privileged helper to the app over binder.
 * Streams are bridged through pipes created lazily on first request.
 */
class ShellProcessBinder(
    private val process: Process,
    private val onFinished: (ShellProcessBinder) -> Unit
) : IShellProcess.Stub() {

    @Volatile
    private var inputPipe: ParcelFileDescriptor? = null

    @Volatile
    private var errorPipe: ParcelFileDescriptor? = null

    @Volatile
    private var outputPipe: ParcelFileDescriptor? = null

    @Synchronized
    override fun getInputStream(): ParcelFileDescriptor {
        return inputPipe ?: readablePipe(process.inputStream).also { inputPipe = it }
    }

    @Synchronized
    override fun getErrorStream(): ParcelFileDescriptor {
        return errorPipe ?: readablePipe(process.errorStream).also { errorPipe = it }
    }

    @Synchronized
    override fun getOutputStream(): ParcelFileDescriptor {
        return outputPipe ?: writablePipe().also { outputPipe = it }
    }

    override fun waitFor(): Int {
        val exitCode = process.waitFor()
        onFinished(this)
        return exitCode
    }

    override fun exitValue(): Int = process.exitValue()

    override fun alive(): Boolean = process.isAlive

    override fun destroy() {
        process.destroy()
        onFinished(this)
    }

    private fun readablePipe(source: InputStream): ParcelFileDescriptor {
        val pipe = createPipe()
        PipePump(source, ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])).start()
        return pipe[0]
    }

    private fun writablePipe(): ParcelFileDescriptor {
        val pipe = createPipe()
        PipePump(ParcelFileDescriptor.AutoCloseInputStream(pipe[0]), process.outputStream).start()
        return pipe[1]
    }

    private fun createPipe(): Array<ParcelFileDescriptor> {
        return try {
            ParcelFileDescriptor.createPipe()
        } catch (e: IOException) {
            throw IllegalStateException("Could not create pipe: ${e.message}", e)
        }
    }
}
