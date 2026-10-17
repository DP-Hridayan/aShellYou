package `in`.hridayan.ashell.core.common.domain.provider

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import kotlinx.coroutines.flow.StateFlow

/**
 * A summary of the port-forwarding tunnels, for screens outside the port-forwarding feature.
 *
 * @property runningCount tunnels that are running or waiting to resume.
 * @property needsAttention true when any tunnel has a warning or failed, or tunnels were stopped
 * unexpectedly and the user has not dealt with it yet.
 * @property runningNames display names of the running tunnels, in start order.
 * @property runningByTransport running or waiting device tunnels, counted per transport.
 */
data class TunnelStatusSummary(
    val runningCount: Int = 0,
    val needsAttention: Boolean = false,
    val runningNames: List<String> = emptyList(),
    val runningByTransport: Map<ExternalDeviceTransport, Int> = emptyMap()
) {
    fun runningOn(transport: ExternalDeviceTransport): Int = runningByTransport[transport] ?: 0
}

/**
 * Publishes [TunnelStatusSummary] so that the home screen and other features can show tunnel
 * status without depending on the port-forwarding feature.
 */
interface TunnelStatusProvider {
    val summary: StateFlow<TunnelStatusSummary>
}
