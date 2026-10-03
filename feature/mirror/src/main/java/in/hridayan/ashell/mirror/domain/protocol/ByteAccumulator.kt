package `in`.hridayan.ashell.mirror.domain.protocol

private const val INITIAL_CAPACITY = 64 * 1024
private const val BYTE_MASK = 0xff
private const val BITS_PER_BYTE = 8

/**
 * Collects bytes from reads that split messages at arbitrary points, and hands them back in
 * big-endian fields once enough have arrived. Callers check [available] before every read.
 */
internal class ByteAccumulator {

    private var buffer = ByteArray(INITIAL_CAPACITY)
    private var start = 0
    private var end = 0

    val available: Int get() = end - start

    fun append(bytes: ByteArray) {
        ensureCapacity(bytes.size)
        bytes.copyInto(buffer, destinationOffset = end)
        end += bytes.size
    }

    fun readBytes(count: Int): ByteArray {
        val bytes = buffer.copyOfRange(start, start + count)
        start += count
        return bytes
    }

    fun readUnsignedByte(): Int = buffer[start++].toInt() and BYTE_MASK

    fun readUnsignedShort(): Int = readBigEndian(Short.SIZE_BYTES).toInt()

    fun readInt(): Int = readBigEndian(Int.SIZE_BYTES).toInt()

    fun readLong(): Long = readBigEndian(Long.SIZE_BYTES)

    fun peekUnsignedByte(offset: Int): Int = buffer[start + offset].toInt() and BYTE_MASK

    fun peekUnsignedShort(offset: Int): Int = peekBigEndian(offset, Short.SIZE_BYTES).toInt()

    fun peekInt(offset: Int): Int = peekBigEndian(offset, Int.SIZE_BYTES).toInt()

    private fun readBigEndian(size: Int): Long = peekBigEndian(0, size).also { start += size }

    private fun peekBigEndian(offset: Int, size: Int): Long {
        var value = 0L
        for (index in 0 until size) {
            value = (value shl BITS_PER_BYTE) or (buffer[start + offset + index].toLong() and BYTE_MASK.toLong())
        }
        return value
    }

    private fun ensureCapacity(extra: Int) {
        if (end + extra <= buffer.size) return

        val pending = available
        if (pending + extra > buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, pending + extra))
        }
        buffer.copyInto(buffer, destinationOffset = 0, startIndex = start, endIndex = end)
        start = 0
        end = pending
    }
}
