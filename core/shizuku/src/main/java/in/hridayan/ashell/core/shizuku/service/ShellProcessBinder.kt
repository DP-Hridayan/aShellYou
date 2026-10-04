package `in`.hridayan.ashell.core.shizuku.service

import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import ashell.core.shizuku.IShellProcess
import java.io.IOException
import java.io.InputStream

/**
 * Exposes a child [Process] running in the privileged helper to the app over binder.
 * Streams are bridged through pipes created lazily on first request.
 * The process is destroyed when [clientToken] dies, so it never outlives the app that started it.
 */
class ShellProcessBinder(
    private val process: Process,
    private val clientToken: IBinder?,
    private val onFinished: (ShellProcessBinder) -> Unit
) : IShellProcess.Stub() {

    private val clientDeath = IBinder.DeathRecipient { destroy() }

    @Volatile
    private var inputPipe: ParcelFileDescriptor? = null

    @Volatile
    private var errorPipe: ParcelFileDescriptor? = null

    @Volatile
    private var outputPipe: ParcelFileDescriptor? = null

    init {
        linkToClient()
    }

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
        finish()
        return exitCode
    }

    override fun exitValue(): Int = process.exitValue()

    override fun alive(): Boolean = process.isAlive

    override fun destroy() {
        process.destroy()
        finish()
    }

    private fun linkToClient() {
        val token = clientToken ?: return
        try {
            token.linkToDeath(clientDeath, 0)
        } catch (_: RemoteException) {
            process.destroy()
        }
    }

    private fun finish() {
        runCatching { clientToken?.unlinkToDeath(clientDeath, 0) }
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
