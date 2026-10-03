package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DeviceKey
import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import `in`.hridayan.ashell.mirror.domain.model.InputAction
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ControlBatchTest {

    private val screen = VideoSize(1080, 2400)

    @Test
    fun `only the latest move of each pointer survives`() {
        val messages = listOf(
            touch(InputAction.MOVE, pointer = 0, x = 1),
            touch(InputAction.MOVE, pointer = 1, x = 2),
            touch(InputAction.MOVE, pointer = 0, x = 3),
            touch(InputAction.MOVE, pointer = 1, x = 4)
        )

        assertEquals(
            listOf(touch(InputAction.MOVE, pointer = 0, x = 3), touch(InputAction.MOVE, pointer = 1, x = 4)),
            ControlBatch.coalesce(messages)
        )
    }

    @Test
    fun `a move before a lift is kept, so the finger ends where it really was`() {
        val messages = listOf(
            touch(InputAction.DOWN, pointer = 0, x = 1),
            touch(InputAction.MOVE, pointer = 0, x = 2),
            touch(InputAction.UP, pointer = 0, x = 2),
            touch(InputAction.DOWN, pointer = 0, x = 9),
            touch(InputAction.MOVE, pointer = 0, x = 10)
        )

        assertEquals(messages, ControlBatch.coalesce(messages))
    }

    @Test
    fun `keys and other messages are never dropped or reordered`() {
        val messages = DeviceKey.BACK.pressMessages() +
            touch(InputAction.MOVE, pointer = 0, x = 1) +
            DeviceKey.HOME.pressMessages() +
            touch(InputAction.MOVE, pointer = 0, x = 2)

        val expected = DeviceKey.BACK.pressMessages() +
            DeviceKey.HOME.pressMessages() +
            touch(InputAction.MOVE, pointer = 0, x = 2)

        assertEquals(expected, ControlBatch.coalesce(messages))
    }

    @Test
    fun `a batch is the messages' encodings back to back`() {
        val messages = DeviceKey.POWER.pressMessages()

        val expected = messages.map(ControlMessageEncoder::encode).reduce { acc, bytes -> acc + bytes }

        assertArrayEquals(expected, ControlBatch.encode(messages))
    }

    private fun touch(action: Int, pointer: Long, x: Int) = ControlMessage.InjectTouch(
        action = action,
        pointerId = pointer,
        position = DevicePosition(x, 0, screen),
        pressure = 1f
    )
}
