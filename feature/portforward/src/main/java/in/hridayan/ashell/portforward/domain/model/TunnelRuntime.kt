package `in`.hridayan.ashell.portforward.domain.model

/**
 * Everything known about a tunnel the user has started.
 *
 * @property events the most recent activity, newest first.
 */
data class TunnelRuntime(
    val spec: TunnelSpec,
    val state: TunnelState,
    val stats: TunnelStats,
    val events: List<TunnelEvent>
)
