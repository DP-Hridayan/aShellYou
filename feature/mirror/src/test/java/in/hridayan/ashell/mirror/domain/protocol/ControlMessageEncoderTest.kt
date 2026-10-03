package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import `in`.hridayan.ashell.mirror.domain.model.InputAction
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/** Expected bytes are copied from upstream's `app/tests/test_control_msg_serialize.c` at v4.1. */
class ControlMessageEncoderTest {

    private val screen = VideoSize(1080, 1920)

    @Test
    fun `keycode matches upstream fixture`() {
        val message = ControlMessage.InjectKeycode(
            action = InputAction.UP,
            keyCode = 0x42,
            repeat = 5,
            metaState = 0x41
        )

        assertBytes(
            message,
            0x00, 0x01,
            0x00, 0x00, 0x00, 0x42,
            0x00, 0x00, 0x00, 0x05,
            0x00, 0x00, 0x00, 0x41
        )
    }

    @Test
    fun `text matches upstream fixture`() {
        val header = intArrayOf(0x01, 0x00, 0x00, 0x00, 0x0d)

        assertBytes(ControlMessage.InjectText("hello, world!"), *(header + ascii("hello, world!")))
    }

    @Test
    fun `touch matches upstream fixture`() {
        val message = ControlMessage.InjectTouch(
            action = InputAction.DOWN,
            pointerId = 0x1234567887654321,
            position = DevicePosition(100, 200, screen),
            pressure = 1f,
            actionButton = 1,
            buttons = 1
        )

        assertBytes(
            message,
            0x02, 0x00,
            0x12, 0x34, 0x56, 0x78, 0x87, 0x65, 0x43, 0x21,
            0x00, 0x00, 0x00, 0x64, 0x00, 0x00, 0x00, 0xc8,
            0x04, 0x38, 0x07, 0x80,
            0xff, 0xff,
            0x00, 0x00, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x01
        )
    }

    @Test
    fun `touch pressure below one is unsigned fixed point`() {
        val message = ControlMessage.InjectTouch(
            action = InputAction.MOVE,
            pointerId = 0,
            position = DevicePosition(0, 0, screen),
            pressure = 0.5f
        )

        val bytes = ControlMessageEncoder.encode(message)

        assertEquals(0x80.toByte(), bytes[22])
        assertEquals(0x00.toByte(), bytes[23])
    }

    @Test
    fun `scroll matches upstream fixture`() {
        val message = ControlMessage.InjectScroll(
            position = DevicePosition(260, 1026, screen),
            horizontal = 16f,
            vertical = -16f,
            buttons = 1
        )

        assertBytes(
            message,
            0x03,
            0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x04, 0x02,
            0x04, 0x38, 0x07, 0x80,
            0x7f, 0xff,
            0x80, 0x00,
            0x00, 0x00, 0x00, 0x01
        )
    }

    @Test
    fun `back or screen on matches upstream fixture`() {
        assertBytes(ControlMessage.BackOrScreenOn(InputAction.UP), 0x04, 0x01)
    }

    @Test
    fun `set clipboard matches upstream fixture`() {
        val message = ControlMessage.SetClipboard(
            sequence = 0x0102030405060708,
            text = "hello, world!",
            paste = true
        )

        val header = intArrayOf(
            0x09,
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x01,
            0x00, 0x00, 0x00, 0x0d
        )

        assertBytes(message, *(header + ascii("hello, world!")))
    }

    @Test
    fun `set display power matches upstream fixture`() {
        assertBytes(ControlMessage.SetDisplayPower(on = true), 0x0a, 0x01)
    }

    @Test
    fun `type only messages are one byte`() {
        assertBytes(ControlMessage.ExpandNotificationPanel, 0x05)
        assertBytes(ControlMessage.ExpandSettingsPanel, 0x06)
        assertBytes(ControlMessage.CollapsePanels, 0x07)
        assertBytes(ControlMessage.RotateDevice, 0x0b)
        assertBytes(ControlMessage.ResetVideo, 0x11)
    }

    @Test
    fun `text longer than the server limit is cut on a character boundary`() {
        val text = "a".repeat(299) + "é"

        val bytes = ControlMessageEncoder.encode(ControlMessage.InjectText(text))

        val length = bytes.copyOfRange(1, 5).fold(0) { acc, b -> (acc shl 8) or (b.toInt() and 0xff) }
        assertEquals(299, length)
        assertEquals(5 + 299, bytes.size)
    }

    private fun ascii(text: String): IntArray = text.map { it.code }.toIntArray()

    private fun assertBytes(message: ControlMessage, vararg expected: Int) {
        assertArrayEquals(
            ByteArray(expected.size) { expected[it].toByte() },
            ControlMessageEncoder.encode(message)
        )
    }
}
