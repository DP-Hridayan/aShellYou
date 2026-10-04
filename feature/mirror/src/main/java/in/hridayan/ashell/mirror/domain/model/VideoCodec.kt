package `in`.hridayan.ashell.mirror.domain.model

/**
 * A video codec the scrcpy server can encode with.
 *
 * @property serverName the value of the server's `video_codec` option.
 * @property streamId the codec id the server writes at the start of the video stream.
 * @property mimeType the MIME type a platform decoder is looked up by.
 * @property displayName the codec's standard name, the same in every language.
 */
enum class VideoCodec(val serverName: String, val streamId: Int, val mimeType: String, val displayName: String) {
    H264("h264", 0x68_32_36_34, "video/avc", "H.264"),
    H265("h265", 0x68_32_36_35, "video/hevc", "H.265"),
    AV1("av1", 0x00_61_76_31, "video/av01", "AV1");

    companion object {
        fun fromStreamId(id: Int): VideoCodec? = entries.firstOrNull { it.streamId == id }
    }
}
