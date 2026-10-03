package `in`.hridayan.ashell.mirror.domain.protocol

import java.io.ByteArrayOutputStream

private const val NEWLINE = '\n'.code.toByte()
private const val CARRIAGE_RETURN = '\r'

/**
 * Splits a shell stream into text lines. Bytes are held until a newline arrives, so neither a line
 * nor a multi-byte character is broken by where a read happened to end. Carriage returns are
 * dropped because a shell with a pseudo terminal ends lines with CRLF.
 */
class LineSplitter {

    private val pending = ByteArrayOutputStream()

    fun feed(chunk: ByteArray): List<String> {
        val lines = mutableListOf<String>()
        for (byte in chunk) {
            if (byte == NEWLINE) {
                lines += pending.toString(Charsets.UTF_8.name()).trimEnd(CARRIAGE_RETURN)
                pending.reset()
            } else {
                pending.write(byte.toInt())
            }
        }
        return lines
    }
}
