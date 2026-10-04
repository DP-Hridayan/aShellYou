package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

/**
 * What a probe learned about the device being mirrored before its server started. Any field is
 * null when the probe could not tell, and a session never waits for one.
 *
 * @property serial the device's `ro.serialno`, the key its encoder limit is remembered under.
 * @property screen the logical display size the server captures, in the display's natural
 * orientation. It is the override size when one is set.
 * @property refreshRate the highest rate the display runs at, after any cap the user set on the
 * device. Capture cannot produce frames faster than this.
 */
data class TargetDevice(
    val serial: String? = null,
    val screen: VideoSize? = null,
    val refreshRate: Int? = null
) {
    companion object {
        val UNKNOWN = TargetDevice()
    }
}

/**
 * This phone, as the place the video is shown.
 *
 * @property screen the display's size in pixels.
 * @property refreshRate the display's highest refresh rate at its current resolution.
 * @property supportedCodecs the codecs this phone has any decoder for.
 */
data class ViewerProfile(
    val screen: VideoSize,
    val refreshRate: Int,
    val decoder: DecoderCapability,
    val supportedCodecs: Set<VideoCodec>
)

/**
 * Whether this phone's decoder for a codec keeps up with a video size at a frame rate. Vendors often
 * overstate this, so it is a ceiling for choosing, not a promise; stream statistics show the truth.
 */
fun interface DecoderCapability {
    fun canDecode(codec: VideoCodec, size: VideoSize, fps: Int): Boolean
}
