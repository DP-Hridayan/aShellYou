package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import ashell.core.shizuku.IShellProcess
import ashell.core.shizuku.IShellUserService

class FakeShellUserService(
    private val uid: Int = 2000,
    private val onNewProcess: (Array<String>) -> IShellProcess? = { null }
) : IShellUserService {
    override fun asBinder(): IBinder? = null
    override fun destroy() = Unit
    override fun newProcess(cmd: Array<String>, env: Array<String>?, dir: String?): IShellProcess? =
        onNewProcess(cmd)

    override fun getUid(): Int = uid
}

class FakeShizukuGateway(
    var binderAlive: Boolean = true,
    var permissionGranted: Boolean = true
) : ShizukuGateway {
    var bindCount = 0
    var unbindCount = 0
    var callbacks: ShizukuGateway.BindCallbacks? = null
    var deadListener: (() -> Unit)? = null
    var serviceAlive = true
    var throwOnBind: Throwable? = null
    var binderAfterRequest = false
    var requestCount = 0
    var receivedListener: (() -> Unit)? = null

    override fun isBinderAlive(): Boolean = binderAlive
    override fun isPermissionGranted(): Boolean = permissionGranted
    override fun isServiceAlive(service: IShellUserService): Boolean = serviceAlive

    override fun bind(callbacks: ShizukuGateway.BindCallbacks) {
        bindCount++
        throwOnBind?.let { throw it }
        this.callbacks = callbacks
    }

    override fun unbind() {
        unbindCount++
    }

    override fun addBinderDeadListener(listener: () -> Unit) {
        deadListener = listener
    }

    override fun addBinderReceivedListener(listener: () -> Unit) {
        receivedListener = listener
    }

    override suspend fun requestBinder(): Boolean {
        requestCount++
        if (binderAfterRequest) binderAlive = true
        return binderAfterRequest
    }

    fun serverChanged() = requireNotNull(receivedListener).invoke()

    fun connect(service: IShellUserService) = requireNotNull(callbacks).onConnected(service)
    fun disconnect() = requireNotNull(callbacks).onDisconnected()
}
