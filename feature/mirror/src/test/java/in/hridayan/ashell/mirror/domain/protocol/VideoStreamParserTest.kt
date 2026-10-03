package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.model.VideoStreamEvent
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.random.Random

private const val SESSION_FLAG = 1L shl 63
private const val CONFIG_FLAG = 1L shl 62
private const val KEY_FRAME_FLAG = 1L shl 61
private const val DEVICE_NAME_FIELD = 64

class VideoStreamParserTest {

    private val config = byteArrayOf(0, 0, 0, 1, 0x67, 0x42)
    private val keyFrame = byteArrayOf(0, 0, 0, 1, 0x65, 1, 2, 3)

    @Test
    fun `parses a full first socket in order`() {
        val events = VideoStreamParser(expectsHandshake = true).feed(firstSocketStream())

        assertEquals(VideoStreamEvent.DeviceName("Pixel 7"), events[0])
        assertEquals(VideoStreamEvent.Codec(VideoCodec.H264), events[1])
        assertEquals(VideoStreamEvent.Session(VideoSize(1080, 2400)), events[2])

        val configPacket = (events[3] as VideoStreamEvent.Packet).packet
        assertTrue(configPacket.isConfig)
        assertArrayEquals(config, configPacket.data)

        val framePacket = (events[4] as VideoStreamEvent.Packet).packet
        assertFalse(framePacket.isConfig)
        assertTrue(framePacket.isKeyFrame)
        assertEquals(33_000L, framePacket.ptsMicros)
        assertArrayEquals(keyFrame, framePacket.data)
        assertEquals(5, events.size)
    }

    @Test
    fun `byte by byte delivery yields the same events`() {
        val stream = firstSocketStream()
        val parser = VideoStreamParser(expectsHandshake = true)

        val events = stream.flatMap { parser.feed(byteArrayOf(it)) }

        assertEquals(5, events.size)
        assertEquals(VideoStreamEvent.Session(VideoSize(1080, 2400)), events[2])
    }

    @Test
    fun `random chunking never changes the result`() {
        val stream = firstSocketStream()
        val random = Random(seed = 7)

        repeat(200) {
            val parser = VideoStreamParser(expectsHandshake = true)
            val events = randomChunks(stream, random).flatMap { parser.feed(it) }
            assertEquals(5, events.size)
            assertArrayEquals(keyFrame, (events[4] as VideoStreamEvent.Packet).packet.data)
        }
    }

    @Test
    fun `a rotation mid stream announces the new session size`() {
        val parser = VideoStreamParser(expectsHandshake = true)
        parser.feed(firstSocketStream())

        val events = parser.feed(sessionPacket(2400, 1080) + mediaPacket(CONFIG_FLAG, config))

        assertEquals(VideoStreamEvent.Session(VideoSize(2400, 1080)), events[0])
        assertTrue((events[1] as VideoStreamEvent.Packet).packet.isConfig)
    }

    @Test
    fun `codec id zero means the server disabled the stream`() {
        val events = VideoStreamParser(expectsHandshake = false).feed(int(0))

        assertEquals(VideoStreamEvent.Disabled(isError = false), events.single())
    }

    @Test
    fun `codec id one means a configuration error`() {
        val events = VideoStreamParser(expectsHandshake = false).feed(int(1))

        assertEquals(VideoStreamEvent.Disabled(isError = true), events.single())
    }

    @Test
    fun `an unknown codec id is reported rather than guessed`() {
        val events = VideoStreamParser(expectsHandshake = false).feed(int(0x00767038))

        assertEquals(
            VideoStreamEvent.Disabled(isError = true, unknownCodecId = 0x00767038),
            events.single()
        )
    }

    @Test
    fun `device name stops at the first nul`() {
        val events = VideoStreamParser(expectsHandshake = true).feed(handshake("Galaxy Tab Ä"))

        assertEquals(VideoStreamEvent.DeviceName("Galaxy Tab Ä"), events.single())
    }

    @Test(expected = VideoStreamParser.ProtocolException::class)
    fun `a wrong dummy byte is rejected`() {
        VideoStreamParser(expectsHandshake = true).feed(byteArrayOf(7))
    }

    private fun firstSocketStream(): ByteArray =
        handshake("Pixel 7") +
            int(VideoCodec.H264.streamId) +
            sessionPacket(1080, 2400) +
            mediaPacket(CONFIG_FLAG, config) +
            mediaPacket(KEY_FRAME_FLAG or 33_000L, keyFrame)

    private fun handshake(name: String): ByteArray {
        val field = ByteArray(DEVICE_NAME_FIELD)
        val nameBytes = name.toByteArray(Charsets.UTF_8)
        nameBytes.copyInto(field)
        return byteArrayOf(0) + field
    }

    private fun sessionPacket(width: Int, height: Int): ByteArray =
        ByteBuffer.allocate(12)
            .putInt((SESSION_FLAG ushr 32).toInt())
            .putInt(width)
            .putInt(height)
            .array()

    private fun mediaPacket(ptsAndFlags: Long, payload: ByteArray): ByteArray =
        ByteBuffer.allocate(12).putLong(ptsAndFlags).putInt(payload.size).array() + payload

    private fun int(value: Int): ByteArray = ByteBuffer.allocate(4).putInt(value).array()

    private fun randomChunks(bytes: ByteArray, random: Random): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < bytes.size) {
            val size = random.nextInt(1, 20).coerceAtMost(bytes.size - offset)
            chunks += bytes.copyOfRange(offset, offset + size)
            offset += size
        }
        return chunks
    }

    private operator fun ByteArray.plus(other: ByteArray): ByteArray =
        ByteArrayOutputStream().apply {
            write(this@plus)
            write(other)
        }.toByteArray()
}
