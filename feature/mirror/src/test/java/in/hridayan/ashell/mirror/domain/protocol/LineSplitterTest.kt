package `in`.hridayan.ashell.mirror.domain.protocol

import org.junit.Assert.assertEquals
import org.junit.Test

class LineSplitterTest {

    @Test
    fun `lines split across reads are joined and carriage returns dropped`() {
        val splitter = LineSplitter()

        val first = splitter.feed("[server] INFO: Dev".toByteArray())
        val second = splitter.feed("ice: Pixel\r\n[server] ERROR: x\nrest".toByteArray())

        assertEquals(emptyList<String>(), first)
        assertEquals(listOf("[server] INFO: Device: Pixel", "[server] ERROR: x"), second)
    }

    @Test
    fun `a multi byte character split across reads survives`() {
        val bytes = "café\n".toByteArray(Charsets.UTF_8)
        val splitter = LineSplitter()

        val lines = splitter.feed(bytes.copyOfRange(0, 4)) + splitter.feed(bytes.copyOfRange(4, bytes.size))

        assertEquals(listOf("café"), lines)
    }
}
