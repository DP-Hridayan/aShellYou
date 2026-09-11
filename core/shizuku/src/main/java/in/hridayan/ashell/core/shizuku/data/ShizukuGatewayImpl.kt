package `in`.hridayan.ashell.core.shizuku.data

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.IBinder
import ashell.core.shizuku.IShellUserService
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.shizuku.service.ShellUserService
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

private const val SERVICE_TAG = "ashell_shell_service"
private const val PROCESS_NAME_SUFFIX = "shizuku_shell"

@Singleton
class ShizukuGatewayImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val binderRequester: ShizukuBinderRequester
) : ShizukuGateway {

    private val userServiceArgs: Shizuku.UserServiceArgs by lazy { buildUserServiceArgs() }

    @Volatile
    private var activeConnection: ServiceConnection? = null

    override fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    override fun isPermissionGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    override fun isServiceAlive(service: IShellUserService): Boolean =
        runCatching { service.asBinder().pingBinder() }.getOrDefault(false)

    override fun bind(callbacks: ShizukuGateway.BindCallbacks) {
        val connection = callbacks.toServiceConnection()
        activeConnection = connection
        Shizuku.bindUserService(userServiceArgs, connection)
    }

    override fun unbind() {
        val connection = activeConnection ?: return
        activeConnection = null
        runCatching { Shizuku.unbindUserService(userServiceArgs, connection, false) }
    }

    override fun addBinderDeadListener(listener: () -> Unit) {
        Shizuku.addBinderDeadListener { listener() }
    }

    override fun addBinderReceivedListener(listener: () -> Unit) {
        Shizuku.addBinderReceivedListener { listener() }
    }

    override suspend fun requestBinder(): Boolean =
        binderRequester.requestPreferringStock() && isBinderAlive()

    private fun ShizukuGateway.BindCallbacks.toServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = binder?.let { IShellUserService.Stub.asInterface(it) }
                if (service != null) onConnected(service) else onDisconnected()
            }

            override fun onServiceDisconnected(name: ComponentName?) = onDisconnected()
        }
    }

    private fun buildUserServiceArgs(): Shizuku.UserServiceArgs {
        val component = ComponentName(context.packageName, ShellUserService::class.java.name)
        return Shizuku.UserServiceArgs(component)
            .daemon(false)
            .processNameSuffix(PROCESS_NAME_SUFFIX)
            .tag(SERVICE_TAG)
            .debuggable(isDebuggable())
            .version(versionCode())
    }

    private fun isDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun versionCode(): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return info.longVersionCode.toInt()
    }
}
