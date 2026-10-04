package `in`.hridayan.ashell.shell.common.data.adb

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ShellProtocolV2Test {

    @Test
    fun `a banner listing shell_v2 supports it`() {
        val banner = "device::ro.product.name=x;ro.product.model=y;features=cmd,shell_v2,stat_v2"
        assertTrue(ShellProtocolV2.supportedBy(banner))
    }

    @Test
    fun `a banner without shell_v2 does not support it`() {
        assertFalse(ShellProtocolV2.supportedBy("device::ro.product.name=x;features=cmd,stat_v2"))
    }

    @Test
    fun `a feature merely containing the name does not count`() {
        assertFalse(ShellProtocolV2.supportedBy("device::features=shell_v2_beta"))
    }

    @Test
    fun `an old banner with no feature list does not support it`() {
        assertFalse(ShellProtocolV2.supportedBy("device::"))
        assertFalse(ShellProtocolV2.supportedBy(""))
    }

    @Test
    fun `the pid preamble runs before the command`() {
        assertEquals(
            "echo __ASHELL_PID__\$\$; cd '/sdcard' && ls",
            ShellProtocolV2.withPidPreamble("cd '/sdcard' && ls")
        )
    }

    @Test
    fun `the kill targets the process group and falls back to the process`() {
        assertEquals(
            "kill -s HUP -- -1234 2>/dev/null || kill -s HUP 1234 2>/dev/null",
            ShellProtocolV2.killCommand(1234)
        )
    }

    @Test
    fun `close stdin is an empty packet with its id`() {
        assertArrayEquals(byteArrayOf(4, 0, 0, 0, 0), ShellProtocolV2.closeStdinPacket())
    }

    @Test
    fun `a whole packet decodes in one feed`() {
        val packets = ShellPacketDecoder().feed(packet(ID_STDOUT, "hi\n"))

        assertEquals(1, packets.size)
        assertEquals("hi\n", String((packets.single() as ShellPacket.Stdout).data))
    }

    @Test
    fun `a header split across feeds is reassembled`() {
        val bytes = packet(ID_STDERR, "oops")
        val decoder = ShellPacketDecoder()

        assertTrue(decoder.feed(bytes.copyOfRange(0, 3)).isEmpty())
        val packets = decoder.feed(bytes.copyOfRange(3, bytes.size))

        assertEquals("oops", String((packets.single() as ShellPacket.Stderr).data))
    }

    @Test
    fun `several packets in one feed all decode in order`() {
        val packets = ShellPacketDecoder().feed(
            packet(ID_STDOUT, "a") + packet(ID_STDERR, "b") + byteArrayOf(ID_EXIT, 1, 0, 0, 0, 7)
        )

        assertEquals(3, packets.size)
        assertEquals("a", String((packets[0] as ShellPacket.Stdout).data))
        assertEquals("b", String((packets[1] as ShellPacket.Stderr).data))
        assertEquals(ShellPacket.Exit(7), packets[2])
    }

    @Test
    fun `an exit code is read as unsigned`() {
        val packets = ShellPacketDecoder().feed(byteArrayOf(ID_EXIT, 1, 0, 0, 0, -1))
        assertEquals(ShellPacket.Exit(255), packets.single())
    }

    @Test
    fun `unknown packet ids are skipped`() {
        val packets = ShellPacketDecoder().feed(packet(UNKNOWN_ID, "zz") + packet(ID_STDOUT, "x"))
        assertEquals("x", String((packets.single() as ShellPacket.Stdout).data))
    }

    @Test(expected = IOException::class)
    fun `an impossible length is rejected`() {
        ShellPacketDecoder().feed(byteArrayOf(ID_STDOUT, -1, -1, -1, -1))
    }

    @Test
    fun `a character split across feeds is decoded whole`() {
        val bytes = "ছবি\n".toByteArray()
        val assembler = ShellLineAssembler(isError = false)

        assertTrue(assembler.feed(bytes.copyOfRange(0, 2)).isEmpty())
        assertEquals(listOf(OutputLine("ছবি")), assembler.feed(bytes.copyOfRange(2, bytes.size)))
    }

    @Test
    fun `carriage returns before a newline are dropped`() {
        assertEquals(
            listOf(OutputLine("a"), OutputLine("b")),
            ShellLineAssembler(isError = false).feed("a\r\nb\n".toByteArray())
        )
    }

    @Test
    fun `an unterminated last line comes out on finish`() {
        val assembler = ShellLineAssembler(isError = true)

        assertTrue(assembler.feed("tail".toByteArray()).isEmpty())
        assertEquals(listOf(OutputLine("tail", isError = true)), assembler.finish())
    }

    @Test
    fun `finish with nothing pending emits nothing`() {
        assertTrue(ShellLineAssembler(isError = false).finish().isEmpty())
    }

    @Test
    fun `the reader consumes the pid line and keeps the output`() {
        val reader = ShellV2OutputReader()

        val lines = reader.feed(packet(ID_STDOUT, "__ASHELL_PID__4321\nAndroid\nDCIM\n"))

        assertEquals(4321, reader.processId)
        assertEquals(listOf(OutputLine("Android"), OutputLine("DCIM")), lines)
    }

    @Test
    fun `the pid line is found even when it arrives in pieces`() {
        val reader = ShellV2OutputReader()

        assertTrue(reader.feed(packet(ID_STDOUT, "__ASHELL_")).isEmpty())
        val lines = reader.feed(packet(ID_STDOUT, "PID__77\nok\n"))

        assertEquals(77, reader.processId)
        assertEquals(listOf(OutputLine("ok")), lines)
    }

    @Test
    fun `stderr lines are marked as errors`() {
        val lines = ShellV2OutputReader().feed(packet(ID_STDERR, "ls: x: No such file\n"))
        assertEquals(listOf(OutputLine("ls: x: No such file", isError = true)), lines)
    }

    @Test
    fun `output without a pid line is passed through untouched`() {
        val reader = ShellV2OutputReader()

        val lines = reader.feed(packet(ID_STDOUT, "plain\n"))

        assertNull(reader.processId)
        assertEquals(listOf(OutputLine("plain")), lines)
    }

    @Test
    fun `the exit packet finishes the reader`() {
        val reader = ShellV2OutputReader()

        reader.feed(packet(ID_STDOUT, "__ASHELL_PID__1\nlast") + byteArrayOf(ID_EXIT, 1, 0, 0, 0, 0))

        assertTrue(reader.isFinished)
        assertEquals(0, reader.exitCode)
        assertEquals(listOf(OutputLine("last")), reader.finish())
    }

    private fun packet(id: Byte, text: String): ByteArray {
        val payload = text.toByteArray()
        val size = payload.size
        return byteArrayOf(
            id,
            (size and 0xFF).toByte(),
            (size shr 8 and 0xFF).toByte(),
            (size shr 16 and 0xFF).toByte(),
            (size shr 24 and 0xFF).toByte()
        ) + payload
    }

    private companion object {
        const val ID_STDOUT: Byte = 1
        const val ID_STDERR: Byte = 2
        const val ID_EXIT: Byte = 3
        const val UNKNOWN_ID: Byte = 9
    }
}
