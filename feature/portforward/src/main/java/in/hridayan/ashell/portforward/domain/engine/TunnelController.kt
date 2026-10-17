package `in`.hridayan.ashell.portforward.domain.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import kotlinx.coroutines.flow.StateFlow

/**
 * Starts, stops and reports on tunnels. It owns every running tunnel, so there is exactly one.
 *
 * A tunnel that could not start, or stopped on its own, stays in [runtimes] in a failed state until
 * it is started again or [dismiss]ed, so the user can see why.
 */
interface TunnelController {

    /** Every started tunnel, keyed by tunnel id. */
    val runtimes: StateFlow<Map<String, TunnelRuntime>>

    /** Starts [spec], restarting it when it is already running. */
    suspend fun start(spec: TunnelSpec): Result<Unit>

    suspend fun stop(id: String)

    /** Stops every running tunnel and returns the ones it stopped. */
    suspend fun stopAll(): List<TunnelSpec>

    /** Forgets a failed tunnel's state. */
    fun dismiss(id: String)
}
