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
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

private const val SERVICE_TAG = "ashell_shell_service"
private const val PROCESS_NAME_SUFFIX = "shizuku_shell"

@Singleton
class ShizukuGatewayImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val binderRequester: ShizukuBinderRequester
) : ShizukuGateway {

    private val helperVersion: Int by lazy { computeHelperVersion() }

    @Volatile
    private var activeBinding: ActiveBinding? = null

    override fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    override fun serverBinder(): IBinder? = runCatching { Shizuku.getBinder() }.getOrNull()

    override fun isPermissionGranted(): Boolean = runCatching {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    override fun isServiceAlive(service: IShellUserService): Boolean =
        runCatching { service.asBinder().pingBinder() }.getOrDefault(false)

    override fun bind(callbacks: ShizukuGateway.BindCallbacks, keepAlive: Boolean) {
        val binding = ActiveBinding(buildUserServiceArgs(keepAlive), callbacks.toServiceConnection())
        activeBinding = binding
        Shizuku.bindUserService(binding.args, binding.connection)
    }

    override fun unbind() {
        val binding = activeBinding ?: return
        activeBinding = null
        runCatching { Shizuku.unbindUserService(binding.args, binding.connection, true) }
    }

    override fun addBinderDeadListener(listener: () -> Unit) {
        Shizuku.addBinderDeadListener { listener() }
    }

    override fun addBinderReceivedListener(listener: () -> Unit) {
        Shizuku.addBinderReceivedListener { listener() }
    }

    override fun addPermissionGrantedListener(listener: () -> Unit) {
        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) listener()
        }
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

    private fun buildUserServiceArgs(keepAlive: Boolean): Shizuku.UserServiceArgs {
        val component = ComponentName(context.packageName, ShellUserService::class.java.name)
        return Shizuku.UserServiceArgs(component)
            .daemon(keepAlive)
            .processNameSuffix(PROCESS_NAME_SUFFIX)
            .tag(SERVICE_TAG)
            .debuggable(isDebuggable())
            .version(helperVersion)
    }

    private fun isDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    /**
     * Shizuku replaces a running helper only when this version changes. Debug reinstalls keep the
     * same version code, so the install time is mixed in to keep a kept-alive helper from serving
     * stale code.
     */
    private fun computeHelperVersion(): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return Objects.hash(info.longVersionCode, info.lastUpdateTime)
    }

    private class ActiveBinding(
        val args: Shizuku.UserServiceArgs,
        val connection: ServiceConnection
    )
}
