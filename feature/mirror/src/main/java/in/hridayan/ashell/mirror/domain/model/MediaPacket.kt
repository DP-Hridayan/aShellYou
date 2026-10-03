package `in`.hridayan.ashell.mirror.domain.model

/**
 * One encoded unit from the video stream.
 *
 * @property ptsMicros the presentation time in microseconds; 0 for config packets.
 * @property isConfig true for codec configuration data (SPS/PPS and the like), which a decoder must
 * receive before any frame of the same capture session.
 *
 * Not a data class: generated equality would compare [data] by reference, which looks structural
 * but is not.
 */
@Suppress("UseDataClass")
class MediaPacket(
    val ptsMicros: Long,
    val isConfig: Boolean,
    val isKeyFrame: Boolean,
    val data: ByteArray
)
