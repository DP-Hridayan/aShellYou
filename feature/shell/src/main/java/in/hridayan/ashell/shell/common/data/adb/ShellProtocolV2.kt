package `in`.hridayan.ashell.shell.common.data.adb

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction

private const val FEATURE_SHELL_V2 = "shell_v2"
private const val FEATURES_KEY = "features="
private const val BANNER_PROPERTIES_SEPARATOR = "::"
private const val PID_MARKER = "__ASHELL_PID__"
private const val HEADER_SIZE = 5
private const val ID_STDOUT = 1
private const val ID_STDERR = 2
private const val ID_EXIT = 3
private const val ID_CLOSE_STDIN = 4
private const val BYTE_MASK = 0xFF
private const val DECODER_FLUSH_CHARS = 4

/**
 * The `shell,v2` service, which runs a command without a terminal and frames its output so stdout,
 * stderr and the exit code arrive separately.
 *
 * The legacy `shell:` service always runs the command in a terminal, which makes tools such as `ls`
 * switch to columns and escape spaces. Devices older than Android 7 lack v2, so callers check
 * [supportedBy] and keep the legacy service for them.
 */
internal object ShellProtocolV2 {

    const val SERVICE_PREFIX = "shell,v2,raw:"

    /** True when the connect [banner] lists the `shell_v2` feature. */
    fun supportedBy(banner: String): Boolean =
        banner.substringAfter(BANNER_PROPERTIES_SEPARATOR, missingDelimiterValue = "")
            .split(';')
            .firstOrNull { it.startsWith(FEATURES_KEY) }
            ?.removePrefix(FEATURES_KEY)
            ?.split(',')
            ?.contains(FEATURE_SHELL_V2) == true

    /**
     * Prints the shell's process id before [command] runs, so an abort can stop the whole process
     * group. Without a terminal, closing the stream only signals the shell itself.
     */
    fun withPidPreamble(command: String): String = "echo $PID_MARKER\$\$; $command"

    /**
     * Hangs up the process group led by [pid], falling back to the process alone when it does not lead
     * a group. Naming the group by the shell's own pid can never reach an unrelated group.
     */
    fun killCommand(pid: Int): String =
        "kill -s HUP -- -$pid 2>/dev/null || kill -s HUP $pid 2>/dev/null"

    /** Ends the command's stdin, so a command that reads it sees end of input instead of waiting. */
    fun closeStdinPacket(): ByteArray = byteArrayOf(ID_CLOSE_STDIN.toByte(), 0, 0, 0, 0)

    internal fun pidFrom(line: String): Int? =
        line.takeIf { it.startsWith(PID_MARKER) }?.removePrefix(PID_MARKER)?.toIntOrNull()
}

internal sealed interface ShellPacket {

    @JvmInline
    value class Stdout(val data: ByteArray) : ShellPacket

    @JvmInline
    value class Stderr(val data: ByteArray) : ShellPacket

    data class Exit(val code: Int) : ShellPacket
}

/**
 * Reassembles v2 packets from stream reads. Each packet is a one-byte id, a little-endian 32-bit
 * length, then the payload, and its boundaries do not line up with the reads that carry it.
 */
internal class ShellPacketDecoder {

    private var pending = ByteArray(0)

    fun feed(chunk: ByteArray): List<ShellPacket> {
        pending += chunk
        val packets = mutableListOf<ShellPacket>()
        var offset = 0

        while (pending.size - offset >= HEADER_SIZE) {
            val length = lengthAt(offset + 1)
            val payloadStart = offset + HEADER_SIZE
            if (pending.size - payloadStart < length) break

            val id = pending[offset].toInt() and BYTE_MASK
            packetOf(id, pending.copyOfRange(payloadStart, payloadStart + length))?.let(packets::add)
            offset = payloadStart + length
        }

        pending = pending.copyOfRange(offset, pending.size)
        return packets
    }

    private fun lengthAt(index: Int): Int {
        val length = ByteBuffer.wrap(pending, index, Int.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
        if (length < 0) throw IOException("Malformed shell packet length")
        return length
    }

    private fun packetOf(id: Int, payload: ByteArray): ShellPacket? = when (id) {
        ID_STDOUT -> ShellPacket.Stdout(payload)
        ID_STDERR -> ShellPacket.Stderr(payload)
        ID_EXIT -> ShellPacket.Exit(payload.firstOrNull()?.toInt()?.and(BYTE_MASK) ?: 0)
        else -> null
    }
}

/**
 * Splits one output stream into lines. Decoding keeps state between feeds, because a multi-byte
 * character can be split across packets.
 */
internal class ShellLineAssembler(private val isError: Boolean) {

    private val decoder = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
    private val text = StringBuilder()
    private var undecoded = ByteArray(0)

    fun feed(bytes: ByteArray): List<OutputLine> {
        decode(undecoded + bytes, endOfInput = false)
        return completeLines()
    }

    fun finish(): List<OutputLine> {
        decode(undecoded, endOfInput = true)
        val flushed = CharBuffer.allocate(DECODER_FLUSH_CHARS)
        decoder.flush(flushed)
        text.append(flushed.flip())

        val lines = completeLines().toMutableList()
        if (text.isNotEmpty()) lines += lineOf(text.toString())
        text.clear()
        return lines
    }

    private fun decode(bytes: ByteArray, endOfInput: Boolean) {
        val input = ByteBuffer.wrap(bytes)
        val output = CharBuffer.allocate(bytes.size + 1)
        decoder.decode(input, output, endOfInput)
        text.append(output.flip())
        undecoded = ByteArray(input.remaining()).also { input.get(it) }
    }

    private fun completeLines(): List<OutputLine> {
        val lines = mutableListOf<OutputLine>()
        var newline = text.indexOf("\n")
        while (newline >= 0) {
            lines += lineOf(text.substring(0, newline))
            text.delete(0, newline + 1)
            newline = text.indexOf("\n")
        }
        return lines
    }

    private fun lineOf(raw: String) = OutputLine(raw.trimEnd('\r'), isError = isError)
}

/**
 * Turns the bytes of a `shell,v2` stream into output lines, consuming the pid line that
 * [ShellProtocolV2.withPidPreamble] adds.
 */
internal class ShellV2OutputReader {

    private val packets = ShellPacketDecoder()
    private val stdout = ShellLineAssembler(isError = false)
    private val stderr = ShellLineAssembler(isError = true)
    private var awaitingPid = true

    var processId: Int? = null
        private set

    var exitCode: Int? = null
        private set

    val isFinished: Boolean get() = exitCode != null

    fun feed(chunk: ByteArray): List<OutputLine> = packets.feed(chunk).flatMap { packet ->
        when (packet) {
            is ShellPacket.Stdout -> consumePidLine(stdout.feed(packet.data))
            is ShellPacket.Stderr -> stderr.feed(packet.data)
            is ShellPacket.Exit -> {
                exitCode = packet.code
                emptyList()
            }
        }
    }

    fun finish(): List<OutputLine> = consumePidLine(stdout.finish()) + stderr.finish()

    private fun consumePidLine(lines: List<OutputLine>): List<OutputLine> {
        if (!awaitingPid || lines.isEmpty()) return lines
        awaitingPid = false

        val pid = ShellProtocolV2.pidFrom(lines.first().text) ?: return lines
        processId = pid
        return lines.drop(1)
    }
}
