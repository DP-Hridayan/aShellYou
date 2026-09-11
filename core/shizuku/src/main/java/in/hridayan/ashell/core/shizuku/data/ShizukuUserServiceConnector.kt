package `in`.hridayan.ashell.core.shizuku.data

import ashell.core.shizuku.IShellUserService
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

private const val BIND_TIMEOUT_MS = 15_000L
private const val UNKNOWN_UID = -1

/**
 * Owns the single bound instance of the privileged helper service and rebinds it on demand.
 *
 * When the Shizuku binder is missing it asks the installed managers for one (stock first).
 * When a different server delivers a binder, the helper bound to the previous server is dropped.
 * A helper start failure is remembered until the server changes so callers can fall back
 * immediately instead of waiting for the bind timeout on every command.
 */
@Singleton
class ShizukuUserServiceConnector @Inject constructor(
    private val gateway: ShizukuGateway,
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
        val bindError = runCatching { gateway.bind(callbacks) }.exceptionOrNull()
        if (bindError != null) return fail(ShizukuServiceError.BindFailed(bindError))
        return awaitConnection(deferred)
    }

    private suspend fun awaitConnection(
        deferred: CompletableDeferred<IShellUserService>
    ): Result<IShellUserService> {
        val service = try {
            withTimeoutOrNull(BIND_TIMEOUT_MS) { deferred.await() }
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

    private fun onServerChanged() {
        val previous = boundService
        boundService = null
        rememberedHelperError = null
        pendingBind?.completeExceptionally(ShizukuServiceError.BinderDied)
        pendingBind = null
        previous?.let { runCatching { it.destroy() } }
        gateway.unbind()
        _state.value = ShizukuServiceState.Idle
    }

    private fun uidOf(service: IShellUserService): Int =
        runCatching { service.uid }.getOrDefault(UNKNOWN_UID)

    private fun ShizukuServiceError.isHelperStartFailure(): Boolean =
        this is ShizukuServiceError.BindTimeout || this is ShizukuServiceError.BindFailed
}
