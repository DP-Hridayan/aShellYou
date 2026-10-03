package `in`.hridayan.ashell.mirror.domain.model

/**
 * The capture settings a session starts the server with. The server fixes them for its lifetime, so
 * changing any of them means starting a new session.
 *
 * @property maxSize the longest edge of the video in pixels, or 0 for the device's native size.
 * @property videoBitRate the encoder's target bitrate in bits per second.
 * @property maxFps an upper bound on frames per second. The stream is variable rate, so a still
 * screen sends far fewer.
 * @property videoEncoder a specific device encoder to use, or null for the device's default.
 */
data class MirrorOptions(
    val maxSize: Int,
    val videoBitRate: Int,
    val maxFps: Int,
    val videoCodec: VideoCodec = VideoCodec.H264,
    val videoEncoder: String? = null
)
