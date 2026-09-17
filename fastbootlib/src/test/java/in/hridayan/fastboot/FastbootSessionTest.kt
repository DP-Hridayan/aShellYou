package `in`.hridayan.fastboot

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class FastbootSessionTest {

    private val image = ByteArray(IMAGE_BYTES) { it.toByte() }

    @Test
    fun `an INFO packet before DATA does not fail the download`() {
        val transport = FakeTransport("INFOerasing flash", DATA_READY, "OKAY", "OKAY")

        val response = session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(ResponseStatus.OKAY, response.status)
        assertArrayEquals(image, transport.payload())
    }

    @Test
    fun `several INFO packets before DATA are all consumed`() {
        val transport = FakeTransport("INFOone", "INFOtwo", "INFOthree", DATA_READY, "OKAY", "OKAY")

        val response = session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(ResponseStatus.OKAY, response.status)
    }

    @Test
    fun `INFO text before the final answer is kept with the result`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "INFOwriting boot", "OKAYdone")

        val response = session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(ResponseStatus.OKAY, response.status)
        assertEquals("writing boot\ndone", response.data)
    }

    @Test
    fun `a refused download returns the failure without sending the image`() {
        val transport = FakeTransport("FAILnot enough memory")

        val response = session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(ResponseStatus.FAIL, response.status)
        assertEquals("not enough memory", response.data)
        assertEquals(listOf("download:00000040"), transport.commands())
    }

    @Test
    fun `a device that accepts a different size fails instead of desynchronising`() {
        val transport = FakeTransport("DATA00000010")

        val failure = assertThrows(FastbootException::class.java) {
            session(transport).sendWithData(FLASH_BOOT, source(), null)
        }

        assertTrue(failure.message!!.contains("16"))
    }

    @Test
    fun `a malformed data size fails`() {
        val transport = FakeTransport("DATAnothex")

        assertThrows(FastbootException::class.java) {
            session(transport).sendWithData(FLASH_BOOT, source(), null)
        }
    }

    @Test
    fun `the command follows the image and the download size is hex`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "OKAY")

        session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(listOf("download:00000040", FLASH_BOOT), transport.commands())
    }

    @Test
    fun `stages are reported in order`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "OKAY")
        val listener = RecordingListener()

        session(transport).sendWithData(FLASH_BOOT, source(), listener)

        assertEquals(listOf(FastbootStage.DOWNLOADING, FastbootStage.WRITING), listener.stages)
        assertEquals(IMAGE_BYTES.toLong(), listener.lastSent)
    }

    @Test
    fun `a failed download confirmation stops before the command is sent`() {
        val transport = FakeTransport(DATA_READY, "FAILchecksum")

        val response = session(transport).sendWithData(FLASH_BOOT, source(), null)

        assertEquals(ResponseStatus.FAIL, response.status)
        assertEquals(listOf("download:00000040"), transport.commands())
    }

    @Test
    fun `an abort during streaming stops the transfer`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "OKAY")
        var active = true
        val session = FastbootSession(transport) {
            if (!active) throw FastbootException("Operation was cancelled")
        }
        active = false

        assertThrows(FastbootException::class.java) {
            session.sendWithData(FLASH_BOOT, source(), null)
        }
    }

    @Test
    fun `a short image fails rather than leaving the device waiting`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "OKAY")
        val truncated = object : FastbootDataSource {
            override val name = "short.img"
            override val length = IMAGE_BYTES.toLong()
            override fun open(): InputStream = ByteArrayInputStream(ByteArray(IMAGE_BYTES / 2))
        }

        val failure = assertThrows(FastbootException::class.java) {
            session(transport).sendWithData(FLASH_BOOT, truncated, null)
        }

        assertTrue(failure.message!!.contains("before the promised length"))
    }

    @Test
    fun `an unreadable image is reported as a fastboot failure`() {
        val transport = FakeTransport(DATA_READY, "OKAY", "OKAY")
        val broken = object : FastbootDataSource {
            override val name = "broken.img"
            override val length = IMAGE_BYTES.toLong()
            override fun open(): InputStream = throw IOException("gone")
        }

        assertThrows(FastbootException::class.java) {
            session(transport).sendWithData(FLASH_BOOT, broken, null)
        }
    }

    @Test
    fun `a plain command collects INFO and returns the final packet`() {
        val transport = FakeTransport("INFOslot a", "OKAYb")

        val response = session(transport).send("getvar:current-slot")

        assertEquals(ResponseStatus.OKAY, response.status)
        assertEquals("slot a\nb", response.data)
    }

    @Test
    fun `an unknown response status fails`() {
        val transport = FakeTransport("WHATever")

        assertThrows(FastbootException::class.java) { session(transport).send("getvar:x") }
    }

    private fun session(transport: FakeTransport) = FastbootSession(transport) { }

    private fun source(): FastbootDataSource = object : FastbootDataSource {
        override val name = "recovery.img"
        override val length = IMAGE_BYTES.toLong()
        override fun open(): InputStream = ByteArrayInputStream(image)
    }

    private class RecordingListener : FastbootTransferListener {
        val stages = mutableListOf<FastbootStage>()
        var lastSent = 0L

        override fun onStage(stage: FastbootStage) {
            stages += stage
        }

        override fun onProgress(bytesSent: Long, totalBytes: Long) {
            lastSent = bytesSent
        }
    }

    private class FakeTransport(vararg packets: String) : FastbootTransport {
        private val responses = ArrayDeque(packets.toList())
        private val writes = mutableListOf<ByteArray>()

        override fun writeFully(data: ByteArray, offset: Int, length: Int) {
            writes += data.copyOfRange(offset, offset + length)
        }

        override fun readPacket(buffer: ByteArray): Int {
            val next = responses.removeFirstOrNull()
                ?: throw FastbootException("The device sent no further packets")
            val bytes = next.toByteArray(Charsets.US_ASCII)
            bytes.copyInto(buffer)
            return bytes.size
        }

        override fun close() = Unit

        fun commands(): List<String> = writes
            .filter { it.isAscii() }
            .map { String(it, Charsets.US_ASCII) }

        fun payload(): ByteArray = writes
            .filterNot { it.isAscii() }
            .fold(ByteArray(0)) { acc, bytes -> acc + bytes }

        private fun ByteArray.isAscii(): Boolean =
            isNotEmpty() && all { it in MIN_PRINTABLE..MAX_PRINTABLE }
    }

    private companion object {
        const val IMAGE_BYTES = 64
        const val FLASH_BOOT = "flash:boot"
        const val DATA_READY = "DATA00000040"
        const val MIN_PRINTABLE: Byte = 0x20
        const val MAX_PRINTABLE: Byte = 0x7E
    }
}
