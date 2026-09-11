package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import android.os.ParcelFileDescriptor
import ashell.core.shizuku.IShellProcess
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import moe.shizuku.server.IRemoteProcess
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fallback for servers where the UserService helper cannot be started: uses the server's
 * still-supported `newProcess` binder call directly through the public AIDL proxy.
 */
fun interface LegacyProcessStarter {
    fun start(command: Array<String>, environment: Array<String>?, workingDirectory: String?): IShellProcess
}

@Singleton
class ShizukuLegacyProcessStarter @Inject constructor() : LegacyProcessStarter {

    override fun start(
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): IShellProcess {
        val binder = Shizuku.getBinder() ?: throw ShizukuServiceError.BinderMissing
        val remote = IShizukuService.Stub.asInterface(binder)
            .newProcess(command, environment, workingDirectory)
            ?: throw ShizukuServiceError.ProcessStartFailed(null)
        return RemoteProcessAdapter(remote)
    }

    private class RemoteProcessAdapter(private val remote: IRemoteProcess) : IShellProcess {
        override fun asBinder(): IBinder = remote.asBinder()
        override fun getInputStream(): ParcelFileDescriptor? = remote.inputStream
        override fun getErrorStream(): ParcelFileDescriptor? = remote.errorStream
        override fun getOutputStream(): ParcelFileDescriptor? = remote.outputStream
        override fun waitFor(): Int = remote.waitFor()
        override fun exitValue(): Int = remote.exitValue()
        override fun alive(): Boolean = remote.alive()
        override fun destroy() = remote.destroy()
    }
}
