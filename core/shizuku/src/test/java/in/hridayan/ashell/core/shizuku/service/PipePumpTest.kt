package `in`.hridayan.ashell.core.shizuku.service

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PipePumpTest {

    private class ClosableSource(bytes: ByteArray) : ByteArrayInputStream(bytes) {
        var closed = false
        override fun close() {
            closed = true
            super.close()
        }
    }

    private class ClosableSink : ByteArrayOutputStream() {
        var closed = false
        override fun close() {
            closed = true
            super.close()
        }
    }

    @Test
    fun `copies every byte from source to sink`() {
        val payload = ByteArray(20_000) { (it % 251).toByte() }
        val source = ClosableSource(payload)
        val sink = ClosableSink()

        PipePump(source, sink).apply {
            start()
            join()
        }

        assertArrayEquals(payload, sink.toByteArray())
    }

    @Test
    fun `closes both ends after the copy finishes`() {
        val source = ClosableSource(byteArrayOf(1, 2, 3))
        val sink = ClosableSink()

        PipePump(source, sink).apply {
            start()
            join()
        }

        assertTrue(source.closed)
        assertTrue(sink.closed)
    }
}
