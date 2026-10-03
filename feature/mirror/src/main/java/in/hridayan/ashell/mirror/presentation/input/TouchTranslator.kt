package `in`.hridayan.ashell.mirror.presentation.input

import android.view.MotionEvent
import `in`.hridayan.ashell.mirror.domain.geometry.VideoViewport
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import `in`.hridayan.ashell.mirror.domain.model.InputAction

/**
 * Turns the mirror view's [MotionEvent]s into per-pointer touch messages for the device.
 *
 * A pointer is only forwarded if it went down on the video or within its edge clamp zone; the
 * pointers being forwarded are tracked so their moves and lifts follow even after they slide off the
 * video. Android batches moves to at most one event per frame, so no further coalescing is needed.
 */
class TouchTranslator {

    private val tracked = mutableSetOf<Int>()

    fun translate(event: MotionEvent, viewport: VideoViewport): List<ControlMessage> =
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tracked.clear()
                listOfNotNull(start(event, event.actionIndex, viewport))
            }

            MotionEvent.ACTION_POINTER_DOWN -> listOfNotNull(start(event, event.actionIndex, viewport))

            MotionEvent.ACTION_MOVE -> (0 until event.pointerCount).mapNotNull { index ->
                continuation(event, index, viewport, InputAction.MOVE)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP ->
                listOfNotNull(lift(event, event.actionIndex, viewport))

            MotionEvent.ACTION_CANCEL -> (0 until event.pointerCount).mapNotNull { index ->
                lift(event, index, viewport)
            }

            else -> emptyList()
        }

    private fun start(event: MotionEvent, index: Int, viewport: VideoViewport): ControlMessage? {
        val position = viewport.mapStart(event.getX(index), event.getY(index)) ?: return null
        val pointerId = event.getPointerId(index)
        tracked += pointerId
        return touch(InputAction.DOWN, pointerId, position, event.getPressure(index))
    }

    private fun lift(event: MotionEvent, index: Int, viewport: VideoViewport): ControlMessage? =
        continuation(event, index, viewport, InputAction.UP)
            .also { tracked -= event.getPointerId(index) }

    private fun continuation(
        event: MotionEvent,
        index: Int,
        viewport: VideoViewport,
        action: Int
    ): ControlMessage? {
        val pointerId = event.getPointerId(index)
        if (pointerId !in tracked) return null
        val position = viewport.mapContinuation(event.getX(index), event.getY(index))
        return touch(action, pointerId, position, event.getPressure(index))
    }

    private fun touch(
        action: Int,
        pointerId: Int,
        position: DevicePosition,
        pressure: Float
    ) = ControlMessage.InjectTouch(
        action = action,
        pointerId = pointerId.toLong(),
        position = position,
        pressure = pressure.coerceIn(0f, 1f)
    )
}
