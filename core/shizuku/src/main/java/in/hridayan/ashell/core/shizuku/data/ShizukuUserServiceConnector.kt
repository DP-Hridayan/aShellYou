package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import ashell.core.shizuku.IShellUserService
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuHelperPolicy
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

private const val BIND_TIMEOUT_MS = 15_000L
private const val UNKNOWN_UID = -1

/**
 * Owns the single bound instance of the privileged helper service and rebinds it on demand.
 *
 * When the Shizuku binder is missing it asks the installed managers for one (stock first).
 * When a different server delivers a binder, the helper bound to the previous server is dropped.
 * A helper start failure is remembered until the server changes so callers can fall back
 * immediately instead of waiting for the bind timeout on every command. A failure of the server's
 * legacy `newProcess` call is remembered the same way, so callers stop trying that path.
 */
@Singleton
class ShizukuUserServiceConnector @Inject constructor(
    private val gateway: ShizukuGateway,
    private val policy: ShizukuHelperPolicy,
    dispatchers: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val _state = MutableStateFlow<ShizukuServiceState>(ShizukuServiceState.Idle)
    val state: StateFlow<ShizukuServiceState> = _state.asStateFlow()

    private val bindMutex = Mutex()

    @Volatile
    private var boundService: IShellUserService? = null

    @Volatile
    private var pendingBind: CompletableDeferred<IShellUserService>? = null

    @Volatile
    private var rememberedHelperError: ShizukuServiceError? = null

    @Volatile
    private var legacyStartFailed = false

    @Volatile
    private var serverAtBind: IBinder? = null

    private var warmUpJob: Job? = null

    /**
     * False once the server's legacy `newProcess` call has failed; reset when the server changes.
     */
    val isLegacyStartUsable: Boolean
        get() = !legacyStartFailed

    init {
        gateway.addBinderDeadListener { onBinderDied() }
        gateway.addBinderReceivedListener { onServerChanged() }
    }

    suspend fun service(): Result<IShellUserService> = bindMutex.withLock {
        val preflight = preflightError()
        val liveService = boundService?.takeIf { gateway.isServiceAlive(it) }
        val rememberedError = rememberedHelperError
        when {
            preflight != null -> Result.failure(preflight)
            liveService != null -> Result.success(liveService)
            rememberedError != null -> Result.failure(rememberedError)
            else -> bind()
        }
    }

    /**
     * The bound helper if it is connected and answering, without waiting on a bind in progress.
     */
    fun readyService(): IShellUserService? = boundService?.takeIf { gateway.isServiceAlive(it) }

    /**
     * Checks that a server binder is available (asking the managers for one if needed) and that
     * permission is granted, without binding the helper.
     */
    suspend fun preflight(): Result<Unit> =
        preflightError()?.let { Result.failure(it) } ?: Result.success(Unit)

    /**
     * Starts binding the helper in the background unless a bind is already running.
     */
    @Synchronized
    fun warmUpInBackground() {
        if (warmUpJob?.isActive == true) return
        warmUpJob = scope.launch { service() }
    }

    fun onLegacyStartFailed() {
        legacyStartFailed = true
    }

    /**
     * Stops the helper and its processes. The next command or warm-up starts a new one.
     */
    fun releaseHelper() = dropHelper()

    private suspend fun preflightError(): ShizukuServiceError? = when {
        !ensureBinder() -> ShizukuServiceError.BinderMissing
        !gateway.isPermissionGranted() -> ShizukuServiceError.PermissionDenied
        else -> null
    }

    private suspend fun ensureBinder(): Boolean = gateway.isBinderAlive() || gateway.requestBinder()

    private suspend fun bind(): Result<IShellUserService> {
        val deferred = CompletableDeferred<IShellUserService>()
        pendingBind = deferred
        boundService = null
        _state.value = ShizukuServiceState.Binding
        serverAtBind = gateway.serverBinder()
        val keepAlive = policy.keepAlive.first()
        val bindError = runCatching { gateway.bind(callbacks, keepAlive) }.exceptionOrNull()
        if (bindError != null) return fail(ShizukuServiceError.BindFailed(bindError))
        return awaitConnection(deferred)
    }

    private suspend fun awaitConnection(
        deferred: CompletableDeferred<IShellUserService>
    ): Result<IShellUserService> {
        val service = try {
            withTimeoutOrNull(BIND_TIMEOUT_MS.milliseconds) { deferred.await() }
        } catch (e: ShizukuServiceError) {
            return fail(e)
        }
        return if (service == null) fail(ShizukuServiceError.BindTimeout) else Result.success(service)
    }

    private fun fail(error: ShizukuServiceError): Result<IShellUserService> {
        pendingBind = null
        if (error.isHelperStartFailure()) rememberedHelperError = error
        _state.value = ShizukuServiceState.Unavailable(error)
        return Result.failure(error)
    }

    private val callbacks = object : ShizukuGateway.BindCallbacks {
        override fun onConnected(service: IShellUserService) = onServiceConnected(service)
        override fun onDisconnected() = onServiceLost(ShizukuServiceError.BinderDied)
    }

    private fun onServiceConnected(service: IShellUserService) {
        boundService = service
        rememberedHelperError = null
        _state.value = ShizukuServiceState.Ready(uidOf(service))
        pendingBind?.complete(service)
        pendingBind = null
    }

    private fun onBinderDied() {
        onServiceLost(ShizukuServiceError.BinderDied)
        scope.launch { gateway.requestBinder() }
    }

    private fun onServiceLost(error: ShizukuServiceError) {
        boundService = null
        pendingBind?.completeExceptionally(error)
        pendingBind = null
        _state.value = ShizukuServiceState.Unavailable(error)
    }

    /**
     * The same server can deliver its binder more than once (its own push at app start and the
     * reply to [ShizukuGateway.requestBinder]); only a different server invalidates the helper.
     */
    private fun onServerChanged() {
        if (isSameServerAsBound()) return
        rememberedHelperError = null
        legacyStartFailed = false
        dropHelper()
    }

    private fun dropHelper() {
        val previous = boundService
        boundService = null
        pendingBind?.completeExceptionally(ShizukuServiceError.BinderDied)
        pendingBind = null
        previous?.let { runCatching { it.destroy() } }
        gateway.unbind()
        _state.value = ShizukuServiceState.Idle
    }

    private fun isSameServerAsBound(): Boolean {
        val current = gateway.serverBinder() ?: return false
        return current === serverAtBind
    }

    private fun uidOf(service: IShellUserService): Int =
        runCatching { service.uid }.getOrDefault(UNKNOWN_UID)

    private fun ShizukuServiceError.isHelperStartFailure(): Boolean =
        this is ShizukuServiceError.BindTimeout || this is ShizukuServiceError.BindFailed
}
