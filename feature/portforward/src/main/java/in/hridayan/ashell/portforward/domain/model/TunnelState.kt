package `in`.hridayan.ashell.portforward.domain.model

/** The live state of a tunnel that the user has started. A stopped tunnel has no state. */
sealed interface TunnelState {
    data object Starting : TunnelState

    /** Listening, with no client connected right now. */
    data object Ready : TunnelState

    /** Listening, with at least one client connected. */
    data object Active : TunnelState

    /** Listening, but the most recent connection failed with [error]. */
    data class Warning(val error: TunnelError) : TunnelState

    /**
     * Listening, and refusing clients until [reason] clears.
     *
     * @property deadlineMillis when the tunnel gives up waiting, or null when it waits indefinitely.
     */
    data class Waiting(val reason: WaitReason, val deadlineMillis: Long? = null) : TunnelState

    /** Not listening, because the tunnel could not start or stopped on its own. */
    data class Failed(val error: TunnelError) : TunnelState
}

enum class WaitReason {
    Network,
    Device
}

/** True while the tunnel holds its port, which is every state except [TunnelState.Failed]. */
val TunnelState.isRunning: Boolean
    get() = this !is TunnelState.Failed

/** True when the user should look at the tunnel. */
val TunnelState.needsAttention: Boolean
    get() = this is TunnelState.Warning || this is TunnelState.Waiting || this is TunnelState.Failed
