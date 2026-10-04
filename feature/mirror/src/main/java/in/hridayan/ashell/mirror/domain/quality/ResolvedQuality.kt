package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

/** Everything a quality is resolved from. */
data class QualityRequest(
    val choice: QualityChoice,
    val transport: ExternalDeviceTransport,
    val target: TargetDevice,
    val viewer: ViewerProfile,

    /** The long edge this device's encoder fell back to in an earlier session, if it did. */
    val encoderLimit: Int?
)

/**
 * The settings a session starts with, and why they are no higher.
 *
 * @property expectedSize the video size the server should stream, before encoder alignment, or
 * null when the device's screen is unknown.
 */
data class ResolvedQuality(
    val options: MirrorOptions,
    val expectedSize: VideoSize?,
    val limit: QualityLimit
)

/** The one constraint that capped a resolved quality, shown to the user as its explanation. */
enum class QualityLimit {
    NONE,

    /** The stream matches what the device itself offers. */
    TARGET_SCREEN,

    /** The device's encoder fell back to a smaller size before. */
    TARGET_ENCODER,

    /** This phone's screen is smaller or slower than the device's. */
    VIEWER_SCREEN,

    VIEWER_DECODER,

    /** The transport's frame-rate or bitrate cap. */
    CONNECTION,

    /** The user fixed every value, or picked a preset. */
    CHOSEN
}
