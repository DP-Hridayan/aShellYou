package `in`.hridayan.ashell.mirror.domain.geometry

import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

/**
 * Where the video sits inside its view, aspect-fit and centred, and how view coordinates map to
 * device pixels.
 *
 * @param edgeClampPx how far outside the video a touch may start and still count, snapped onto the
 * edge. This lets the device's own edge gestures start without hitting the exact first pixel.
 */
class VideoViewport(
    viewWidth: Float,
    viewHeight: Float,
    private val video: VideoSize,
    private val edgeClampPx: Float
) {

    private val scale = minOf(viewWidth / video.width, viewHeight / video.height)

    val contentWidth: Float = video.width * scale
    val contentHeight: Float = video.height * scale
    val contentLeft: Float = (viewWidth - contentWidth) / 2
    val contentTop: Float = (viewHeight - contentHeight) / 2

    private val contentRight = contentLeft + contentWidth
    private val contentBottom = contentTop + contentHeight

    /** Maps the first point of a gesture, or returns null when it started outside the clamp zone. */
    fun mapStart(x: Float, y: Float): DevicePosition? {
        val insideX = x in (contentLeft - edgeClampPx)..(contentRight + edgeClampPx)
        val insideY = y in (contentTop - edgeClampPx)..(contentBottom + edgeClampPx)
        return if (insideX && insideY) mapContinuation(x, y) else null
    }

    /** Maps a later point of a gesture already in progress, pinning it to the video's edge. */
    fun mapContinuation(x: Float, y: Float): DevicePosition {
        val deviceX = ((x - contentLeft) / contentWidth * video.width).toInt()
        val deviceY = ((y - contentTop) / contentHeight * video.height).toInt()
        return DevicePosition(
            x = deviceX.coerceIn(0, video.width - 1),
            y = deviceY.coerceIn(0, video.height - 1),
            screenSize = video
        )
    }
}
