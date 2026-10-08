package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import ashell.core.shizuku.IShellUserService

/**
 * Thin seam over the static `rikka.shizuku.Shizuku` API so the connector can be unit tested.
 */
interface ShizukuGateway {
    fun isBinderAlive(): Boolean

    /**
     * The Shizuku server binder currently held, used to tell a new server from a repeated delivery.
     */
    fun serverBinder(): IBinder?
    fun isPermissionGranted(): Boolean
    fun isServiceAlive(service: IShellUserService): Boolean

    /**
     * @param keepAlive whether the helper should keep running after the app process dies
     */
    fun bind(callbacks: BindCallbacks, keepAlive: Boolean)

    /**
     * Unbinds and asks Shizuku to stop the helper.
     */
    fun unbind()
    fun addBinderDeadListener(listener: () -> Unit)
    fun addBinderReceivedListener(listener: () -> Unit)
    fun addPermissionGrantedListener(listener: () -> Unit)

    /**
     * Asks installed Shizuku managers (stock first) to deliver their server binder.
     * @return true when a live binder is available afterwards
     */
    suspend fun requestBinder(): Boolean

    interface BindCallbacks {
        fun onConnected(service: IShellUserService)
        fun onDisconnected()
    }
}
