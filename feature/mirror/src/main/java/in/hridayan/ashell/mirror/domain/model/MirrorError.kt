package `in`.hridayan.ashell.mirror.domain.model

/**
 * Why a session could not start or had to stop. Variants that carry [serverLog] hold the server's
 * last output lines, which is the only diagnosis available from the other device.
 */
sealed interface MirrorError {

    data object NotConnected : MirrorError

    data object DeployFailed : MirrorError

    data object ServerVersionMismatch : MirrorError

    data class EncoderFailed(val serverLog: List<String>) : MirrorError

    data class ServerExited(val serverLog: List<String>) : MirrorError

    data class ConnectTimeout(val serverLog: List<String>) : MirrorError

    /** This phone has no decoder for the codec the server streams. */
    data class DecoderUnavailable(val codec: VideoCodec) : MirrorError

    data object StreamError : MirrorError

    /** The device stopped answering requests partway through starting or streaming. */
    data object DeviceNotResponding : MirrorError
}
