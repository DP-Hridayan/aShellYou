package `in`.hridayan.ashell.core.shizuku.data

import ashell.core.shizuku.IShellUserService

/**
 * Thin seam over the static `rikka.shizuku.Shizuku` API so the connector can be unit tested.
 */
interface ShizukuGateway {
    fun isBinderAlive(): Boolean
    fun isPermissionGranted(): Boolean
    fun isServiceAlive(service: IShellUserService): Boolean
    fun bind(callbacks: BindCallbacks)
    fun unbind()
    fun addBinderDeadListener(listener: () -> Unit)
    fun addBinderReceivedListener(listener: () -> Unit)

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
