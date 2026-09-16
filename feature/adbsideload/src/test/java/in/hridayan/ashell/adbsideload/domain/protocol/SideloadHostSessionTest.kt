package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SideloadHostSessionTest {

    private val file = ByteArray(100) { it.toByte() }
    private val transport = FakeTransport()

    private fun session(maxWriteSize: Int = 65_536): SideloadHostSession {
        transport.maxWriteSize = maxWriteSize
        return SideloadHostSession(
            transport = transport,
            blockReader = InMemoryBlockReader(file),
            tracker = SideloadProgressTracker("rom.zip", file.size.toLong(), BLOCK_SIZE, clock = { 0L }),
            blockSize = BLOCK_SIZE,
            firstRequestTimeoutMs = 1_000L,
        )
    }

    @Test
    fun `serves requested blocks in any order and completes on DONEDONE`() = runTest {
        transport.enqueue("00000001", "00000000", "DONEDONE")

        val emissions = session().run().toList()

        assertEquals(2, transport.written.size)
        assertArrayEquals(file.copyOfRange(64, 100), transport.written[0])
        assertArrayEquals(file.copyOfRange(0, 64), transport.written[1])
        assertEquals(SideloadStatus.WAITING_FOR_RECOVERY, emissions.first().status)
        assertEquals(SideloadStatus.COMPLETE, emissions.last().status)
        assertEquals(1f, emissions.last().progress)
    }

    @Test
    fun `repeated block is served again`() = runTest {
        transport.enqueue("00000000", "00000000", "DONEDONE")

        val emissions = session().run().toList()

        assertEquals(2, transport.written.size)
        assertEquals(1, emissions.sending().last().servedBlocks)
        assertEquals(128L, emissions.sending().last().bytesSent)
    }

    @Test
    fun `two requests in one packet are both served`() = runTest {
        transport.enqueue("0000000000000001", "DONEDONE")

        session().run().toList()

        assertEquals(2, transport.written.size)
    }

    @Test
    fun `block writes are split to the transport maximum`() = runTest {
        transport.enqueue("00000000", "DONEDONE")

        session(maxWriteSize = 4_096).run().toList()

        assertEquals(1, transport.written.size)
    }

    @Test
    fun `large blocks are chunked to the transport maximum`() = runTest {
        val bigFile = ByteArray(20_000) { it.toByte() }
        val bigSession = SideloadHostSession(
            transport = transport.apply { maxWriteSize = 8_192 },
            blockReader = InMemoryBlockReader(bigFile),
            tracker = SideloadProgressTracker("rom.zip", bigFile.size.toLong(), 20_000, clock = { 0L }),
            blockSize = 20_000,
            firstRequestTimeoutMs = 1_000L,
        )
        transport.enqueue("00000000", "DONEDONE")

        bigSession.run().toList()

        assertEquals(listOf(8_192, 8_192, 3_616), transport.written.map { it.size })
    }

    @Test
    fun `no write exceeds the negotiated payload`() = runTest {
        val bigFile = ByteArray(65_536) { it.toByte() }
        val session = SideloadHostSession(
            transport = transport.apply { maxWriteSize = 4_096 },
            blockReader = InMemoryBlockReader(bigFile),
            tracker = SideloadProgressTracker("rom.zip", bigFile.size.toLong(), 65_536, clock = { 0L }),
            blockSize = 65_536,
            firstRequestTimeoutMs = 1_000L,
        )
        transport.enqueue("00000000", "DONEDONE")

        session.run().toList()

        assertEquals(16, transport.written.size)
        assertTrue(transport.written.all { it.size <= 4_096 })
    }

    @Test
    fun `FAILFAIL ends with recovery failure`() = runTest {
        transport.enqueue("00000000", "FAILFAIL")

        val last = session().run().toList().last()

        assertEquals(SideloadStatus.ERROR, last.status)
        assertEquals(SideloadError.RECOVERY_REPORTED_FAILURE, last.error)
    }

    @Test
    fun `out of range block ends with error`() = runTest {
        transport.enqueue("00000009")

        val last = session().run().toList().last()

        assertEquals(SideloadError.BLOCK_OUT_OF_RANGE, last.error)
        assertEquals("9", last.errorDetail)
        assertTrue(transport.written.isEmpty())
    }

    @Test
    fun `malformed frame ends with error`() = runTest {
        transport.enqueue("garbage!")

        val last = session().run().toList().last()

        assertEquals(SideloadError.INVALID_REQUEST, last.error)
    }

    @Test
    fun `closed stream before completion ends with stream closed`() = runTest {
        transport.enqueue("00000000")
        transport.closeIncoming()

        val last = session().run().toList().last()

        assertEquals(SideloadError.STREAM_CLOSED, last.error)
        assertEquals(1, transport.written.size)
    }

    @Test
    fun `no first request within timeout ends with recovery not responding`() = runTest {
        val last = session().run().toList().last()

        assertEquals(SideloadError.RECOVERY_NOT_RESPONDING, last.error)
    }

    @Test
    fun `write failure ends with stream closed`() = runTest {
        transport.enqueue("00000000")
        transport.failWrites = true

        val last = session().run().toList().last()

        assertEquals(SideloadError.STREAM_CLOSED, last.error)
    }

    private fun List<SideloadOperation>.sending() = filter { it.status == SideloadStatus.SENDING }

    private class FakeTransport : SideloadTransport {
        override var maxWriteSize: Int = 65_536
        val written = mutableListOf<ByteArray>()
        var failWrites = false
        private val incoming = Channel<ByteArray>(Channel.UNLIMITED)

        fun enqueue(vararg frames: String) {
            frames.forEach { incoming.trySend(it.toByteArray(Charsets.US_ASCII)) }
        }

        fun closeIncoming() {
            incoming.close()
        }

        override suspend fun read(): ByteArray =
            incoming.receiveCatching().getOrNull() ?: throw IOException("Stream closed")

        override suspend fun write(data: ByteArray) {
            if (failWrites) throw IOException("bulk transfer failed")
            written += data
        }

        override fun close() {
            incoming.close()
        }
    }

    private class InMemoryBlockReader(private val bytes: ByteArray) : SideloadBlockReader {
        override fun readBlock(offset: Long, length: Int): ByteArray =
            bytes.copyOfRange(offset.toInt(), offset.toInt() + length)
    }

    private companion object {
        const val BLOCK_SIZE = 64
    }
}
