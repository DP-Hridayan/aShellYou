package `in`.hridayan.ashell.shell.file_browser.domain.protocol

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class SyncSessionTest {

    @Test
    fun `a download writes every chunk in order`() = runTest {
        val payload = "hello world".toByteArray()
        val transport = FakeTransport(
            frame(SyncProtocol.ID_DATA, payload.size) + payload +
                    SyncProtocol.header(SyncProtocol.ID_DONE, 0)
        )
        val sink = ByteArrayOutputStream()

        SyncSession(transport).pull("/sdcard/a.txt", sink) {}

        assertArrayEquals(payload, sink.toByteArray())
    }

    @Test
    fun `a download reassembles several chunks`() = runTest {
        val transport = FakeTransport(
            frame(SyncProtocol.ID_DATA, 3) + "abc".toByteArray() +
                    frame(SyncProtocol.ID_DATA, 2) + "de".toByteArray() +
                    SyncProtocol.header(SyncProtocol.ID_DONE, 0)
        )
        val sink = ByteArrayOutputStream()

        SyncSession(transport).pull("/sdcard/a.txt", sink) {}

        assertEquals("abcde", sink.toByteArray().toString(StandardCharsets.UTF_8))
    }

    @Test
    fun `a download survives a frame split across reads`() = runTest {
        val body = frame(SyncProtocol.ID_DATA, 4) + "wxyz".toByteArray() +
                SyncProtocol.header(SyncProtocol.ID_DONE, 0)
        val transport = FakeTransport(body, chunkSize = 3)
        val sink = ByteArrayOutputStream()

        SyncSession(transport).pull("/sdcard/a.txt", sink) {}

        assertEquals("wxyz", sink.toByteArray().toString(StandardCharsets.UTF_8))
    }

    @Test
    fun `a download reports running progress`() = runTest {
        val transport = FakeTransport(
            frame(SyncProtocol.ID_DATA, 3) + "abc".toByteArray() +
                    frame(SyncProtocol.ID_DATA, 2) + "de".toByteArray() +
                    SyncProtocol.header(SyncProtocol.ID_DONE, 0)
        )
        val seen = mutableListOf<Long>()

        SyncSession(transport).pull("/sdcard/a.txt", ByteArrayOutputStream()) { seen.add(it) }

        assertEquals(listOf(3L, 5L), seen)
    }

    @Test
    fun `a download failure carries the device message`() = runTest {
        val message = "remote object '/nope' does not exist"
        val transport = FakeTransport(
            frame(SyncProtocol.ID_FAIL, message.length) + message.toByteArray()
        )

        val error = runCatching {
            SyncSession(transport).pull("/nope", ByteArrayOutputStream()) {}
        }.exceptionOrNull()

        assertTrue(error is SyncProtocolException)
        assertEquals(message, error?.message)
    }

    @Test
    fun `an upload sends the data and is acknowledged`() = runTest {
        val transport = FakeTransport(SyncProtocol.header(SyncProtocol.ID_OKAY, 0))
        val source = ByteArrayInputStream("payload".toByteArray())

        SyncSession(transport).push(source, "/sdcard/a.txt") {}

        val written = transport.written()
        assertTrue(written.contains("SEND"))
        assertTrue(written.contains("/sdcard/a.txt,420"))
        assertTrue(written.contains("payload"))
        assertTrue(written.contains("DONE"))
    }

    @Test
    fun `an upload reports running progress`() = runTest {
        val transport = FakeTransport(SyncProtocol.header(SyncProtocol.ID_OKAY, 0))
        val seen = mutableListOf<Long>()

        SyncSession(transport)
            .push(ByteArrayInputStream(ByteArray(10)), "/sdcard/a.bin") { seen.add(it) }

        assertEquals(listOf(10L), seen)
    }

    @Test
    fun `an upload refused by the device raises the device message`() = runTest {
        val message = "Read-only file system"
        val transport = FakeTransport(
            frame(SyncProtocol.ID_FAIL, message.length) + message.toByteArray()
        )

        val error = runCatching {
            SyncSession(transport).push(ByteArrayInputStream(ByteArray(4)), "/system/x") {}
        }.exceptionOrNull()

        assertEquals(message, error?.message)
    }

    @Test
    fun `an empty upload still completes`() = runTest {
        val transport = FakeTransport(SyncProtocol.header(SyncProtocol.ID_OKAY, 0))

        SyncSession(transport).push(ByteArrayInputStream(ByteArray(0)), "/sdcard/empty") {}

        assertTrue(transport.written().contains("DONE"))
    }

    @Test
    fun `stat reports an existing file`() = runTest {
        val transport = FakeTransport(
            SyncProtocol.header(SyncProtocol.ID_STAT, 0) + statPayload(mode = 33188, size = 4096)
        )

        val stat = SyncSession(transport).stat("/sdcard/a.txt")

        assertTrue(stat.exists)
        assertEquals(4096L, stat.size)
    }

    @Test
    fun `stat reports a missing file as absent`() = runTest {
        val transport = FakeTransport(
            SyncProtocol.header(SyncProtocol.ID_STAT, 0) + statPayload(mode = 0, size = 0)
        )

        assertFalse(SyncSession(transport).stat("/nope").exists)
    }

    @Test
    fun `a size above two gigabytes is not read as negative`() = runTest {
        val threeGigabytes = 3L * 1024 * 1024 * 1024
        val transport = FakeTransport(
            SyncProtocol.header(SyncProtocol.ID_STAT, 0) +
                    statPayload(mode = 33188, size = threeGigabytes.toInt())
        )

        assertEquals(threeGigabytes, SyncSession(transport).stat("/sdcard/big").size)
    }

    @Test
    fun `an unexpected message during download is reported`() = runTest {
        val transport = FakeTransport(SyncProtocol.header("WXYZ", 0))

        val error = runCatching {
            SyncSession(transport).pull("/sdcard/a.txt", ByteArrayOutputStream()) {}
        }.exceptionOrNull()

        assertTrue(error is SyncProtocolException)
    }

    @Test
    fun `a stream that ends early fails rather than hanging`() = runTest {
        val transport = FakeTransport(frame(SyncProtocol.ID_DATA, 8) + "half".toByteArray())

        val error = runCatching {
            SyncSession(transport).pull("/sdcard/a.txt", ByteArrayOutputStream()) {}
        }.exceptionOrNull()

        assertTrue(error is IOException)
    }

    private fun frame(id: String, value: Int) = SyncProtocol.header(id, value)

    private fun statPayload(mode: Int, size: Int): ByteArray =
        ByteBuffer.allocate(SyncProtocol.STAT_PAYLOAD_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(mode)
            .putInt(size)
            .putInt(0)
            .array()

    private class FakeTransport(
        private val script: ByteArray,
        private val chunkSize: Int = Int.MAX_VALUE
    ) : SyncTransport {
        private var position = 0
        private val sent = ByteArrayOutputStream()

        override suspend fun read(): ByteArray {
            if (position >= script.size) throw IOException("Stream closed.")
            val take = minOf(chunkSize, script.size - position)
            return script.copyOfRange(position, position + take).also { position += take }
        }

        override suspend fun write(data: ByteArray) {
            sent.write(data)
        }

        override fun close() = Unit

        fun written(): String = sent.toByteArray().toString(StandardCharsets.UTF_8)
    }
}
