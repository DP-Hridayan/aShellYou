package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.DeviceMessage
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class DeviceMessageParserTest {

    @Test
    fun `parses clipboard text`() {
        val text = "copied ✓".toByteArray(Charsets.UTF_8)
        val bytes = byteArrayOf(0) + ByteBuffer.allocate(4).putInt(text.size).array() + text

        val messages = DeviceMessageParser().feed(bytes)

        assertEquals(listOf(DeviceMessage.Clipboard("copied ✓")), messages)
    }

    @Test
    fun `parses clipboard ack`() {
        val bytes = byteArrayOf(1) + ByteBuffer.allocate(8).putLong(42L).array()

        assertEquals(listOf(DeviceMessage.AckClipboard(42L)), DeviceMessageParser().feed(bytes))
    }

    @Test
    fun `parses uhid output`() {
        val bytes = byteArrayOf(2, 0, 3, 0, 2, 9, 8)

        val message = DeviceMessageParser().feed(bytes).single() as DeviceMessage.UhidOutput

        assertEquals(3, message.id)
        assertArrayEquals(byteArrayOf(9, 8), message.data)
    }

    @Test
    fun `messages split across reads are reassembled`() {
        val first = byteArrayOf(1) + ByteBuffer.allocate(8).putLong(1L).array()
        val second = byteArrayOf(1) + ByteBuffer.allocate(8).putLong(2L).array()
        val all = first + second
        val parser = DeviceMessageParser()

        val messages = all.toList().chunked(3).flatMap { parser.feed(it.toByteArray()) }

        assertEquals(
            listOf(DeviceMessage.AckClipboard(1L), DeviceMessage.AckClipboard(2L)),
            messages
        )
    }

    @Test(expected = VideoStreamParser.ProtocolException::class)
    fun `an unknown type is a protocol error`() {
        DeviceMessageParser().feed(byteArrayOf(99))
    }
}
