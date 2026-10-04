package `in`.hridayan.ashell.core.shizuku.data

import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuHelperPolicy
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the helper's lifecycle for the whole app process. It binds the helper as soon as Shizuku is
 * usable and [ShizukuHelperPolicy.warmUpWanted] is true, then binds again after a server change or
 * a permission grant. It also stops a kept-alive helper when the user turns keep-alive off.
 */
@Singleton
class ShizukuWarmUpInitializer @Inject constructor(
    private val connector: ShizukuUserServiceConnector,
    private val gateway: ShizukuGateway,
    private val policy: ShizukuHelperPolicy,
    dispatchers: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val started = AtomicBoolean(false)

    /**
     * Counts binder deliveries and permission grants. The connector state alone cannot trigger a
     * warm-up for them, because it is often already [ShizukuServiceState.Idle] when they happen.
     */
    private val serverEvents = MutableStateFlow(0)

    fun start() {
        if (!started.compareAndSet(false, true)) return

        gateway.addBinderReceivedListener(::onServerEvent)
        gateway.addPermissionGrantedListener(::onServerEvent)

        scope.launch { warmUpWhenWanted() }
        scope.launch { releaseWhenKeepAliveTurnedOff() }
    }

    private fun onServerEvent() = serverEvents.update { it + 1 }

    private suspend fun warmUpWhenWanted() {
        combine(policy.warmUpWanted, connector.state, serverEvents) { wanted, state, _ ->
            wanted && state is ShizukuServiceState.Idle
        }
            .filter { it }
            .collect { connector.warmUpInBackground() }
    }

    private suspend fun releaseWhenKeepAliveTurnedOff() {
        policy.keepAlive
            .distinctUntilChanged()
            .drop(1)
            .filter { keepAlive -> !keepAlive }
            .collect { connector.releaseHelper() }
    }
}
