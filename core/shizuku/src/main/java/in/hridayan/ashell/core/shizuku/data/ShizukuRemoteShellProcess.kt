package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import ashell.core.shizuku.IShellProcess
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val EXIT_CODE_REMOTE_FAILURE = 255

/**
 * [Process] view of a child process living in the privileged helper.
 * Stream and lifecycle calls are forwarded over binder; binder failures surface as [IOException]
 * on streams and as [EXIT_CODE_REMOTE_FAILURE] from [waitFor].
 */
class ShizukuRemoteShellProcess(private val remote: IShellProcess) : Process() {

    @Volatile
    private var helperDied = false

    private val deathRecipient = IBinder.DeathRecipient { helperDied = true }

    private val input: InputStream by lazy {
        ParcelFileDescriptor.AutoCloseInputStream(descriptor { remote.inputStream })
    }

    private val error: InputStream by lazy {
        ParcelFileDescriptor.AutoCloseInputStream(descriptor { remote.errorStream })
    }

    private val output: OutputStream by lazy {
        ParcelFileDescriptor.AutoCloseOutputStream(descriptor { remote.outputStream })
    }

    init {
        runCatching { remote.asBinder().linkToDeath(deathRecipient, 0) }
    }

    override fun getInputStream(): InputStream = input

    override fun getErrorStream(): InputStream = error

    override fun getOutputStream(): OutputStream = output

    override fun waitFor(): Int {
        return try {
            remote.waitFor()
        } catch (_: RemoteException) {
            EXIT_CODE_REMOTE_FAILURE
        }
    }

    override fun exitValue(): Int {
        return try {
            remote.exitValue()
        } catch (_: RemoteException) {
            EXIT_CODE_REMOTE_FAILURE
        }
    }

    override fun isAlive(): Boolean {
        if (helperDied) return false
        return runCatching { remote.alive() }.getOrDefault(false)
    }

    override fun destroy() {
        runCatching { remote.destroy() }
        runCatching { remote.asBinder().unlinkToDeath(deathRecipient, 0) }
    }

    private fun descriptor(fetch: () -> ParcelFileDescriptor?): ParcelFileDescriptor {
        return try {
            fetch() ?: throw IOException("Shizuku helper returned no stream descriptor")
        } catch (e: RemoteException) {
            throw IOException("Shizuku helper stream unavailable: ${e.message}", e)
        }
    }
}
