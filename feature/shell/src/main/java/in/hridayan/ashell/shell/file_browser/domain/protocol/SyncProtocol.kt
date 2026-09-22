package `in`.hridayan.ashell.shell.file_browser.domain.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * The framing used by the ADB `sync:` service, the protocol behind `adb push` and `adb pull`.
 *
 * Every message begins with a four byte ASCII id followed by a little endian 32 bit field. That field
 * is a payload length for most messages, but carries a modification time on the `DONE` that ends an
 * upload, which is why it is named [SyncFrame.value] rather than a length.
 */
object SyncProtocol {

    const val HEADER_SIZE = 8
    private const val ID_LENGTH = 4

    /** The largest payload the protocol allows in a single data message. */
    const val MAX_DATA_CHUNK = 64 * 1024

    const val ID_STAT = "STAT"
    const val ID_RECV = "RECV"
    const val ID_SEND = "SEND"
    const val ID_DATA = "DATA"
    const val ID_DONE = "DONE"
    const val ID_OKAY = "OKAY"
    const val ID_FAIL = "FAIL"
    const val ID_QUIT = "QUIT"

    /**
     * Bytes of a whole `STAT` reply: the id, then mode, size and modification time.
     *
     * Unlike every other reply, the four bytes after the id are data rather than a payload length, so
     * this message must not be read through [decodeHeader]. Doing so consumed the mode as a length
     * and then asked for twelve more bytes on top of the sixteen the device had already sent.
     */
    const val STAT_REPLY_SIZE = 16

    fun header(id: String, value: Int): ByteArray {
        require(id.length == ID_LENGTH) { "A sync id is $ID_LENGTH characters, got '$id'" }
        return ByteBuffer.allocate(HEADER_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(id.toByteArray(StandardCharsets.US_ASCII))
            .putInt(value)
            .array()
    }

    /**
     * A request that carries a path, such as `STAT`, `RECV` or `SEND`.
     */
    fun request(id: String, argument: String): ByteArray {
        val encoded = argument.toByteArray(StandardCharsets.UTF_8)
        return ByteBuffer.allocate(HEADER_SIZE + encoded.size)
            .put(header(id, encoded.size))
            .put(encoded)
            .array()
    }

    fun decodeHeader(bytes: ByteArray): SyncFrame {
        require(bytes.size >= HEADER_SIZE) { "A sync header is $HEADER_SIZE bytes" }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val id = ByteArray(ID_LENGTH).also { buffer.get(it) }
        return SyncFrame(String(id, StandardCharsets.US_ASCII), buffer.int)
    }

    /** The four character id at the front of [reply]. */
    fun idOf(reply: ByteArray): String {
        require(reply.size >= ID_LENGTH) { "A sync id is $ID_LENGTH bytes" }
        return String(reply, 0, ID_LENGTH, StandardCharsets.US_ASCII)
    }

    fun decodeStat(reply: ByteArray): SyncStat {
        require(reply.size >= STAT_REPLY_SIZE) { "A stat reply is $STAT_REPLY_SIZE bytes" }
        val buffer = ByteBuffer.wrap(reply, ID_LENGTH, STAT_REPLY_SIZE - ID_LENGTH)
            .order(ByteOrder.LITTLE_ENDIAN)
        return SyncStat(
            mode = buffer.int,
            size = buffer.int.toUnsignedLong(),
            modifiedAtSeconds = buffer.int.toUnsignedLong()
        )
    }
}

/**
 * @property value a payload length for most messages, and a modification time on the `DONE` that
 * ends an upload.
 */
data class SyncFrame(val id: String, val value: Int)

/**
 * @property size the classic protocol reports this as an unsigned 32 bit value, so a file of 4 GiB or
 * more is reported wrapped. The transfer itself is unaffected, because data messages carry their own
 * lengths.
 */
data class SyncStat(
    val mode: Int,
    val size: Long,
    val modifiedAtSeconds: Long
) {
    val exists: Boolean get() = mode != 0
}

private fun Int.toUnsignedLong(): Long = this.toLong() and 0xFFFFFFFFL
