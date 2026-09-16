package `in`.hridayan.ashell.adbsideload.domain.protocol

import java.io.ByteArrayOutputStream

/**
 * Reassembles the byte stream coming from recovery into fixed 8-byte frames.
 * Recovery writes each block request as a zero-padded decimal number with no terminator,
 * and ends the session with the literal frames `DONEDONE` or `FAILFAIL`.
 */
class SideloadRequestParser {

    private val pending = ByteArrayOutputStream()

    fun feed(bytes: ByteArray): List<SideloadRequest> {
        pending.write(bytes)
        val buffered = pending.toByteArray()
        val frameCount = buffered.size / FRAME_SIZE
        val requests = List(frameCount) { parseFrame(buffered, it * FRAME_SIZE) }
        retainRemainder(buffered, frameCount * FRAME_SIZE)
        return requests
    }

    private fun retainRemainder(buffered: ByteArray, consumed: Int) {
        pending.reset()
        pending.write(buffered, consumed, buffered.size - consumed)
    }

    private fun parseFrame(bytes: ByteArray, offset: Int): SideloadRequest {
        val frame = String(bytes, offset, FRAME_SIZE, Charsets.US_ASCII)
        return when (frame) {
            DONE_FRAME -> SideloadRequest.Done
            FAIL_FRAME -> SideloadRequest.Failed
            else -> parseBlockFrame(frame)
        }
    }

    private fun parseBlockFrame(frame: String): SideloadRequest {
        val index = frame.trim().toIntOrNull()
        return if (index != null && index >= 0) {
            SideloadRequest.Block(index)
        } else {
            SideloadRequest.Malformed(frame)
        }
    }

    companion object {
        const val FRAME_SIZE = 8
        private const val DONE_FRAME = "DONEDONE"
        private const val FAIL_FRAME = "FAILFAIL"
    }
}
