package `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.io.InputStream

private const val FIRST_REPLY_MS = 3000L
private const val IDLE_MS = 500L
private const val POLL_MS = 20L

class AdbReplyReaderTest {

    private var clock = 0L

    private val reader = AdbReplyReader(
        timeouts = ReplyTimeouts(FIRST_REPLY_MS, IDLE_MS, POLL_MS),
        now = { clock },
        pause = { clock += it }
    )

    private fun read(input: InputStream, isCancelled: () -> Boolean = { false }): List<String> =
        reader.lines(input, isCancelled).toList()

    @Test
    fun `a single line reply is returned`() {
        val input = ScriptedStream(listOf(Chunk.Data("restarting in TCP mode port: 5555\n")))
        assertEquals(listOf("restarting in TCP mode port: 5555"), read(input))
    }

    @Test
    fun `a reply without a trailing newline is still returned`() {
        val input = ScriptedStream(listOf(Chunk.Data("adbd is already running as root")))
        assertEquals(listOf("adbd is already running as root"), read(input))
    }

    @Test
    fun `carriage returns are trimmed`() {
        val input = ScriptedStream(listOf(Chunk.Data("one\r\ntwo\r\n")))
        assertEquals(listOf("one", "two"), read(input))
    }

    @Test
    fun `a reply split across chunks is reassembled`() {
        val input = ScriptedStream(
            listOf(Chunk.Data("restarting in "), Chunk.Silence, Chunk.Data("TCP mode\n"))
        )
        assertEquals(listOf("restarting in TCP mode"), read(input))
    }

    @Test
    fun `several lines in one chunk are split`() {
        val input = ScriptedStream(listOf(Chunk.Data("a\nb\nc\n")))
        assertEquals(listOf("a", "b", "c"), read(input))
    }

    @Test
    fun `a service that never replies gives up after the first reply window`() {
        val input = ScriptedStream(emptyList())
        assertEquals(emptyList<String>(), read(input))
        assertTrue(clock >= FIRST_REPLY_MS)
    }

    @Test
    fun `silence after a reply ends the read on the shorter idle window`() {
        val input = ScriptedStream(listOf(Chunk.Data("done\n")))
        assertEquals(listOf("done"), read(input))
        assertTrue(clock >= IDLE_MS)
        assertTrue(clock < FIRST_REPLY_MS)
    }

    @Test
    fun `a socket that dies without closing does not hang`() {
        val input = ScriptedStream(listOf(Chunk.Data("restarting\n"), Chunk.Forever))
        assertEquals(listOf("restarting"), read(input))
    }

    @Test
    fun `a stream closed mid read keeps what already arrived`() {
        val input = ScriptedStream(listOf(Chunk.Data("partial reply"), Chunk.Closed))
        assertEquals(listOf("partial reply"), read(input))
    }

    @Test
    fun `end of stream ends the read`() {
        val input = ScriptedStream(listOf(Chunk.Data("bye\n"), Chunk.EndOfStream))
        assertEquals(listOf("bye"), read(input))
    }

    @Test
    fun `cancellation stops the read`() {
        val input = ScriptedStream(listOf(Chunk.Forever))
        assertEquals(emptyList<String>(), read(input, isCancelled = { true }))
    }

    @Test
    fun `lines are produced lazily rather than after the stream ends`() {
        val input = ScriptedStream(listOf(Chunk.Data("first\n"), Chunk.Forever))
        val firstLine = reader.lines(input) { false }.first()
        assertEquals("first", firstLine)
    }

    private sealed interface Chunk {
        data class Data(val text: String) : Chunk
        data object Silence : Chunk
        data object Closed : Chunk
        data object EndOfStream : Chunk
        data object Forever : Chunk
    }

    private class ScriptedStream(script: List<Chunk>) : InputStream() {
        private val remaining = ArrayDeque(script)
        private var pending: ByteArray = ByteArray(0)
        private var offset = 0
        private var stuck = false

        override fun available(): Int {
            if (offset < pending.size) return pending.size - offset
            if (stuck) return 0
            while (remaining.isNotEmpty()) {
                when (val next = remaining.removeFirst()) {
                    is Chunk.Data -> {
                        pending = next.text.toByteArray()
                        offset = 0
                        return pending.size
                    }

                    Chunk.Silence -> return 0
                    Chunk.Closed -> throw IOException("Stream closed.")
                    Chunk.EndOfStream -> return 0
                    Chunk.Forever -> {
                        stuck = true
                        return 0
                    }
                }
            }
            return 0
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (offset >= pending.size) return -1
            val count = minOf(len, pending.size - offset)
            pending.copyInto(b, off, offset, offset + count)
            offset += count
            return count
        }

        override fun read(): Int = throw UnsupportedOperationException()
    }
}
