package `in`.hridayan.ashell.mirror.domain.model

/** What the video socket carries, in the order the server sends it. */
sealed interface VideoStreamEvent {

    /** The device's model name, sent once on the first socket. */
    data class DeviceName(val name: String) : VideoStreamEvent

    data class Codec(val codec: VideoCodec) : VideoStreamEvent

    /**
     * The stream cannot be used: the server turned it off, reported a configuration error, or named
     * a codec this client does not know.
     *
     * @property isError true when the server reported a configuration error and will stop.
     * @property unknownCodecId the unrecognised codec id, when that was the cause.
     */
    data class Disabled(val isError: Boolean, val unknownCodecId: Int? = null) : VideoStreamEvent

    /**
     * A new capture session began, at start and after every rotation or resize. Frames that follow
     * have this size, and touches must be scaled to it or the server drops them.
     */
    data class Session(val size: VideoSize) : VideoStreamEvent

    data class Packet(val packet: MediaPacket) : VideoStreamEvent
}
