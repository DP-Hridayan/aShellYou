package `in`.hridayan.ashell.mirror.domain.model

/** Where one mirroring session is in its life. */
sealed interface MirrorState {

    data object Idle : MirrorState

    /**
     * @property serverAlreadyDeployed true when the device already held this server version, so the
     * upload was skipped.
     */
    data class Starting(
        val step: StartStep,
        val serverAlreadyDeployed: Boolean = false
    ) : MirrorState

    /**
     * @property videoSize null until the first capture session arrives.
     * @property isControlAvailable false once the device refused input injection; video continues.
     */
    data class Streaming(
        val deviceName: String,
        val videoSize: VideoSize?,
        val isControlAvailable: Boolean
    ) : MirrorState

    /** The link dropped and the session is waiting for the transport to come back. */
    data object Reconnecting : MirrorState

    data class Ended(val reason: EndReason) : MirrorState

    data class Failed(val error: MirrorError) : MirrorState
}

enum class StartStep {
    PREPARING,
    STARTING_SERVER,
    CONNECTING
}

enum class EndReason {
    DEVICE_DISCONNECTED,
    SERVER_STOPPED
}
