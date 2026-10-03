package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.MediaPacket
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.model.VideoStreamEvent
import java.io.IOException

private const val DEVICE_NAME_FIELD_LENGTH = 64
private const val CODEC_ID_LENGTH = 4
private const val HEADER_LENGTH = 12
private const val STREAM_DISABLED = 0
private const val STREAM_CONFIG_ERROR = 1
private const val CONFIG_FLAG = 1L shl 62
private const val KEY_FRAME_FLAG = 1L shl 61
private const val PTS_MASK = KEY_FRAME_FLAG - 1
private const val LOW_WORD_MASK = 0xffff_ffffL
private const val MAX_PACKET_SIZE = 64 * 1024 * 1024

/**
 * Turns the bytes of a scrcpy v4 video socket into [VideoStreamEvent]s, whatever sizes the reads
 * arrive in.
 *
 * The first socket of a forward-tunnel session starts with a zero dummy byte and the 64-byte device
 * name, so [expectsHandshake] must be true for exactly that socket. After the codec id, every
 * 12-byte header is either a session packet (most significant bit set: flags, width, height) or a
 * media packet header (flags and PTS, then the payload size).
 */
class VideoStreamParser(expectsHandshake: Boolean) {

    class ProtocolException(message: String) : IOException(message)

    private enum class Expect { DUMMY_BYTE, DEVICE_NAME, CODEC_ID, HEADER, PAYLOAD, NOTHING }

    private val input = ByteAccumulator()
    private var expect = if (expectsHandshake) Expect.DUMMY_BYTE else Expect.CODEC_ID
    private var pendingPtsAndFlags = 0L
    private var pendingPayloadSize = 0

    @Throws(ProtocolException::class)
    fun feed(chunk: ByteArray): List<VideoStreamEvent> {
        input.append(chunk)
        val events = mutableListOf<VideoStreamEvent>()
        while (step(events)) Unit
        return events
    }

    private fun step(events: MutableList<VideoStreamEvent>): Boolean = when (expect) {
        Expect.DUMMY_BYTE -> readDummyByte()
        Expect.DEVICE_NAME -> readDeviceName(events)
        Expect.CODEC_ID -> readCodecId(events)
        Expect.HEADER -> readHeader(events)
        Expect.PAYLOAD -> readPayload(events)
        Expect.NOTHING -> false
    }

    private fun readDummyByte(): Boolean {
        if (input.available < 1) return false
        val dummy = input.readUnsignedByte()
        if (dummy != 0) throw ProtocolException("Expected the dummy byte, got $dummy")
        expect = Expect.DEVICE_NAME
        return true
    }

    private fun readDeviceName(events: MutableList<VideoStreamEvent>): Boolean {
        if (input.available < DEVICE_NAME_FIELD_LENGTH) return false
        val field = input.readBytes(DEVICE_NAME_FIELD_LENGTH)
        val length = field.indexOf(0).takeIf { it >= 0 } ?: field.size
        events += VideoStreamEvent.DeviceName(String(field, 0, length, Charsets.UTF_8))
        expect = Expect.CODEC_ID
        return true
    }

    private fun readCodecId(events: MutableList<VideoStreamEvent>): Boolean {
        if (input.available < CODEC_ID_LENGTH) return false
        val id = input.readInt()
        val codec = VideoCodec.fromStreamId(id)
        events += when {
            id == STREAM_DISABLED -> VideoStreamEvent.Disabled(isError = false)
            id == STREAM_CONFIG_ERROR -> VideoStreamEvent.Disabled(isError = true)
            codec == null -> VideoStreamEvent.Disabled(isError = true, unknownCodecId = id)
            else -> VideoStreamEvent.Codec(codec)
        }
        expect = if (codec != null) Expect.HEADER else Expect.NOTHING
        return codec != null
    }

    private fun readHeader(events: MutableList<VideoStreamEvent>): Boolean {
        if (input.available < HEADER_LENGTH) return false
        val first = input.readLong()
        val second = input.readInt()

        if (first < 0) {
            val width = (first and LOW_WORD_MASK).toInt()
            events += VideoStreamEvent.Session(VideoSize(width = width, height = second))
            return true
        }

        if (second !in 0..MAX_PACKET_SIZE) throw ProtocolException("Invalid packet size $second")
        pendingPtsAndFlags = first
        pendingPayloadSize = second
        expect = Expect.PAYLOAD
        return true
    }

    private fun readPayload(events: MutableList<VideoStreamEvent>): Boolean {
        if (input.available < pendingPayloadSize) return false
        val packet = MediaPacket(
            ptsMicros = pendingPtsAndFlags and PTS_MASK,
            isConfig = pendingPtsAndFlags and CONFIG_FLAG != 0L,
            isKeyFrame = pendingPtsAndFlags and KEY_FRAME_FLAG != 0L,
            data = input.readBytes(pendingPayloadSize)
        )
        events += VideoStreamEvent.Packet(packet)
        expect = Expect.HEADER
        return true
    }
}
