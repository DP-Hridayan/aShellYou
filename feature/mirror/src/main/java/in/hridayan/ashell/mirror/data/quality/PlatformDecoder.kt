package `in`.hridayan.ashell.mirror.data.quality

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

/**
 * The capabilities of the decoder a session would use for one MIME type, read from the platform's
 * codec list.
 */
internal class PlatformDecoder private constructor(private val caps: MediaCodecInfo.VideoCapabilities) {

    /** Checked in both orientations, because the device can rotate mid-session. */
    fun canDecode(size: VideoSize, fps: Int): Boolean =
        fitsAt(size.width, size.height, fps) || fitsAt(size.height, size.width, fps)

    /**
     * Performance points are the vendor's measured real-time limits, so they win when published.
     * Without them, the level-based check is all there is; it may throw for sizes it can't judge,
     * and those are given the benefit of the doubt.
     */
    private fun fitsAt(width: Int, height: Int, fps: Int): Boolean {
        val alignedWidth = alignDown(width, caps.widthAlignment)
        val alignedHeight = alignDown(height, caps.heightAlignment)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val points = caps.supportedPerformancePoints
            if (!points.isNullOrEmpty()) {
                val wanted = MediaCodecInfo.VideoCapabilities.PerformancePoint(alignedWidth, alignedHeight, fps)
                return points.any { it.covers(wanted) }
            }
        }
        return runCatching { caps.areSizeAndRateSupported(alignedWidth, alignedHeight, fps.toDouble()) }
            .getOrDefault(true)
    }

    private fun alignDown(value: Int, alignment: Int): Int =
        if (alignment <= 1) value else maxOf(alignment, value / alignment * alignment)

    companion object {

        /**
         * The first hardware decoder for [mimeType], as `createDecoderByType` would normally pick, or
         * the first of any kind. Null when this phone can't decode the type at all.
         */
        fun forMimeType(mimeType: String): PlatformDecoder? = runCatching {
            val decoders = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { info ->
                !info.isEncoder && info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
            }
            val preferred = decoders.firstOrNull { it.isHardware() } ?: decoders.firstOrNull()
            preferred?.getCapabilitiesForType(mimeType)?.videoCapabilities?.let(::PlatformDecoder)
        }.getOrNull()

        private fun MediaCodecInfo.isHardware(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isHardwareAccelerated
    }
}
