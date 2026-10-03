package `in`.hridayan.ashell.mirror.domain.protocol

private const val U16_MAX = 0xffff
private const val U16_SCALE = 65536f
private const val I16_MAX = 0x7fff
private const val I16_MIN = -0x8000
private const val I16_SCALE = 32768f
private const val UTF8_CONTINUATION_MASK = 0xc0
private const val UTF8_CONTINUATION_BITS = 0x80

/** Value encodings shared by the scrcpy wire format. */
internal object WireEncoding {

    /** The server rejects a cut inside a multi-byte character, so back off to its first byte. */
    fun utf8TruncationIndex(utf8: ByteArray, maxBytes: Int): Int {
        if (utf8.size <= maxBytes) return utf8.size
        var index = maxBytes
        while (index > 0 && utf8[index].toInt() and UTF8_CONTINUATION_MASK == UTF8_CONTINUATION_BITS) {
            index--
        }
        return index
    }

    /** 0 to 1 as unsigned 16-bit fixed point, with 1 encoded as the maximum value. */
    fun toUnsignedFixedPoint(value: Float): Int =
        if (value >= 1f) U16_MAX else (value * U16_SCALE).toInt().coerceIn(0, U16_MAX)

    /** -1 to 1 as signed 16-bit fixed point, with 1 encoded as the maximum value. */
    fun toSignedFixedPoint(value: Float): Int =
        if (value >= 1f) I16_MAX else (value * I16_SCALE).toInt().coerceIn(I16_MIN, I16_MAX)
}
