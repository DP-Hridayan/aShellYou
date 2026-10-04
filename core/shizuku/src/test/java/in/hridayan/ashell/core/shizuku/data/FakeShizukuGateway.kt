package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import ashell.core.shizuku.IShellProcess
import ashell.core.shizuku.IShellUserService
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuHelperPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.FileDescriptor

open class FakeShellUserService(
    private val uid: Int = 2000,
    private val onNewProcess: (Array<String>) -> IShellProcess? = { null }
) : IShellUserService {
    override fun asBinder(): IBinder? = null
    override fun destroy() = Unit
    override fun newProcess(
        cmd: Array<String>,
        env: Array<String>?,
        dir: String?,
        clientToken: IBinder?
    ): IShellProcess? = onNewProcess(cmd)

    override fun getUid(): Int = uid
}

class FakeShizukuGateway(
    var binderAlive: Boolean = true,
    var permissionGranted: Boolean = true
) : ShizukuGateway {
    var bindCount = 0
    var lastKeepAlive: Boolean? = null
    var unbindCount = 0
    var callbacks: ShizukuGateway.BindCallbacks? = null
    var deadListener: (() -> Unit)? = null
    var serviceAlive = true
    var throwOnBind: Throwable? = null
    var binderAfterRequest = false
    var requestCount = 0
    var server: IBinder? = null
    private val receivedListeners = mutableListOf<() -> Unit>()
    private val permissionListeners = mutableListOf<() -> Unit>()

    override fun isBinderAlive(): Boolean = binderAlive
    override fun serverBinder(): IBinder? = server
    override fun isPermissionGranted(): Boolean = permissionGranted
    override fun isServiceAlive(service: IShellUserService): Boolean = serviceAlive

    override fun bind(callbacks: ShizukuGateway.BindCallbacks, keepAlive: Boolean) {
        bindCount++
        lastKeepAlive = keepAlive
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
        receivedListeners += listener
    }

    override fun addPermissionGrantedListener(listener: () -> Unit) {
        permissionListeners += listener
    }

    override suspend fun requestBinder(): Boolean {
        requestCount++
        if (binderAfterRequest) binderAlive = true
        return binderAfterRequest
    }

    fun serverChanged() {
        server = FakeBinder()
        receivedListeners.forEach { it() }
    }

    fun sameServerDeliveredAgain() = receivedListeners.forEach { it() }

    fun grantPermission() {
        permissionGranted = true
        permissionListeners.forEach { it() }
    }

    fun connect(service: IShellUserService) = requireNotNull(callbacks).onConnected(service)
    fun disconnect() = requireNotNull(callbacks).onDisconnected()
}

class FakeHelperPolicy(
    warmUpWanted: Boolean = false,
    keepAlive: Boolean = false
) : ShizukuHelperPolicy {
    override val warmUpWanted = MutableStateFlow(warmUpWanted)
    override val keepAlive = MutableStateFlow(keepAlive)
}

class FakeBinder : IBinder {
    override fun getInterfaceDescriptor(): String? = null
    override fun pingBinder(): Boolean = true
    override fun isBinderAlive(): Boolean = true
    override fun queryLocalInterface(descriptor: String): IInterface? = null
    override fun dump(fd: FileDescriptor, args: Array<out String>?) = Unit
    override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) = Unit
    override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean = false
    override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) = Unit
    override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean = true
}
