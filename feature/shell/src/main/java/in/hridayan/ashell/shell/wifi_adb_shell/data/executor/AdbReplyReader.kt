package `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor

import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

private const val READ_BUFFER_SIZE = 4096

/**
 * How long to wait at each stage of reading a reply.
 *
 * @property firstReplyMs how long to wait for the first byte, before anything has arrived.
 * @property idleMs how long to wait for more once a reply has started.
 * @property pollIntervalMs how long to pause between checks.
 */
data class ReplyTimeouts(
    val firstReplyMs: Long,
    val idleMs: Long,
    val pollIntervalMs: Long
)

/**
 * Reads an ADB stream line by line without ever parking in a blocking read.
 *
 * `AdbStream.read` waits until either data arrives or the stream is closed. When a daemon restarts,
 * the socket can die without a close ever reaching us, so that wait never ends: the caller's flow
 * never completes and the shell stays busy. Polling instead means silence is bounded, and a stream
 * closed from elsewhere, by an abort, is noticed promptly.
 *
 * Lines are produced lazily, so a caller can emit each one as it arrives rather than holding the
 * whole reply until the stream ends.
 */
class AdbReplyReader(
    private val timeouts: ReplyTimeouts,
    private val now: () -> Long = System::currentTimeMillis,
    private val pause: (Long) -> Unit = { Thread.sleep(it) }
) {

    fun lines(input: InputStream, isCancelled: () -> Boolean): Sequence<String> = sequence {
        val buffer = StringBuilder()
        var sawData = false
        var lastEventMs = now()
        var reading = true

        while (reading && !isCancelled()) {
            val chunk = readChunk(input)
            when {
                chunk == null -> reading = false

                chunk.isEmpty() ->
                    if (hasWaitedLongEnough(lastEventMs, sawData)) {
                        reading = false
                    } else {
                        pause(timeouts.pollIntervalMs)
                    }

                else -> {
                    sawData = true
                    lastEventMs = now()
                    buffer.append(chunk)
                    yieldAll(buffer.takeCompleteLines())
                }
            }
        }

        val remainder = buffer.toString().trimEnd('\r')
        if (remainder.isNotBlank()) yield(remainder)
    }

    /**
     * @return the text read, an empty string when nothing is waiting yet, or null when the stream
     * has ended or been closed.
     */
    private fun readChunk(input: InputStream): String? {
        val available = availableOrNull(input) ?: return null
        if (available <= 0) return ""

        val raw = ByteArray(minOf(available, READ_BUFFER_SIZE))
        val bytesRead = readOrNull(input, raw) ?: return null
        return if (bytesRead < 0) null else String(raw, 0, bytesRead, StandardCharsets.UTF_8)
    }

    private fun hasWaitedLongEnough(lastEventMs: Long, sawData: Boolean): Boolean {
        val limit = if (sawData) timeouts.idleMs else timeouts.firstReplyMs
        return now() - lastEventMs >= limit
    }

    private fun availableOrNull(input: InputStream): Int? = try {
        input.available()
    } catch (_: IOException) {
        null
    }

    private fun readOrNull(input: InputStream, raw: ByteArray): Int? = try {
        input.read(raw, 0, raw.size)
    } catch (_: IOException) {
        null
    }
}

private fun StringBuilder.takeCompleteLines(): List<String> {
    val lines = mutableListOf<String>()
    var newlineIndex = indexOf("\n")
    while (newlineIndex >= 0) {
        lines.add(substring(0, newlineIndex).trimEnd('\r'))
        delete(0, newlineIndex + 1)
        newlineIndex = indexOf("\n")
    }
    return lines
}
