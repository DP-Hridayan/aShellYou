package `in`.hridayan.fastboot

import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Speaks the fastboot protocol over a [FastbootTransport].
 *
 * A device may answer any command with a run of `INFO` packets before the real response, so every
 * read consumes those first and keeps their text with the outcome. Reading a single packet and
 * treating it as final is the difference between a flash that works and one that reports failure
 * before a byte of the image has been sent.
 */
class FastbootSession(
    private val transport: FastbootTransport,
    private val ensureActive: () -> Unit,
) {

    fun send(command: String): FastbootResponse {
        writeCommand(command)
        return readResponse().toResponse()
    }

    /**
     * Downloads [source] into the device's buffer and then runs [command] against it.
     *
     * @return the device's answer to [command], or the first failure that ended the exchange
     */
    fun sendWithData(
        command: String,
        source: FastbootDataSource,
        listener: FastbootTransferListener?,
    ): FastbootResponse = when (val ready = beginDownload(source.length)) {
        is DownloadReady.Refused -> ready.response
        is DownloadReady.Accepted -> transferAndRun(command, source, ready.length, listener)
    }

    private fun transferAndRun(
        command: String,
        source: FastbootDataSource,
        length: Long,
        listener: FastbootTransferListener?,
    ): FastbootResponse {
        listener?.onStage(FastbootStage.DOWNLOADING)
        streamData(source, length, listener)

        val confirmation = readResponse()
        if (confirmation.status != ResponseStatus.OKAY) return confirmation.toResponse()

        listener?.onStage(FastbootStage.WRITING)
        writeCommand(command)
        return readResponse().toResponse()
    }

    private fun beginDownload(length: Long): DownloadReady {
        require(length in 1..MAX_DOWNLOAD_BYTES) { "Unsupported download size: $length" }
        writeCommand(DOWNLOAD_COMMAND + String.format(Locale.US, SIZE_FORMAT, length))

        val ready = readResponse()
        if (ready.status != ResponseStatus.DATA) return DownloadReady.Refused(ready.toResponse())

        val accepted = ready.payload.trim().toLongOrNull(HEX_RADIX)
            ?: throw FastbootException("Device sent a malformed data size: ${ready.payload}")
        if (accepted != length) {
            throw FastbootException("Device accepted $accepted bytes but the image is $length bytes")
        }
        return DownloadReady.Accepted(accepted)
    }

    private fun streamData(source: FastbootDataSource, length: Long, listener: FastbootTransferListener?) {
        val buffer = ByteArray(STREAM_BUFFER_BYTES)
        var sent = 0L
        openSource(source).use { input ->
            while (sent < length) {
                ensureActive()
                val wanted = minOf(buffer.size.toLong(), length - sent).toInt()
                val read = readChunk(input, buffer, wanted, sent)
                transport.writeFully(buffer, 0, read)
                sent += read
                listener?.onProgress(sent, length)
            }
        }
    }

    private fun openSource(source: FastbootDataSource): InputStream = try {
        source.open()
    } catch (e: IOException) {
        throw FastbootException("Could not open the image: ${e.message}", e)
    }

    private fun readChunk(input: InputStream, buffer: ByteArray, wanted: Int, sent: Long): Int {
        val read = try {
            input.read(buffer, 0, wanted)
        } catch (e: IOException) {
            throw FastbootException("Could not read the image at offset $sent: ${e.message}", e)
        }
        if (read <= 0) {
            throw FastbootException("Image ended after $sent bytes, before the promised length")
        }
        return read
    }

    private fun writeCommand(command: String) {
        val bytes = command.toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_COMMAND_BYTES) { "Command too long: $command" }
        transport.writeFully(bytes, 0, bytes.size)
    }

    /** Consumes any leading `INFO` packets and returns the first packet that is not one. */
    private fun readResponse(): CollectedResponse {
        val info = mutableListOf<String>()
        while (true) {
            ensureActive()
            val packet = readPacket()
            if (packet.status != ResponseStatus.INFO) {
                return CollectedResponse(packet.status, packet.payload, info.toList())
            }
            info.add(packet.payload)
        }
    }

    private fun readPacket(): Packet {
        val buffer = ByteArray(MAX_RESPONSE_BYTES)
        val received = transport.readPacket(buffer)
        if (received < STATUS_PREFIX_LENGTH) {
            throw FastbootException("Invalid response of $received bytes")
        }
        val prefix = String(buffer, 0, STATUS_PREFIX_LENGTH, StandardCharsets.UTF_8)
        val status = ResponseStatus.fromPrefix(prefix)
            ?: throw FastbootException("Unknown response status: $prefix")
        return Packet(status, payloadOf(buffer, received))
    }

    private fun payloadOf(buffer: ByteArray, received: Int): String = if (received > STATUS_PREFIX_LENGTH) {
        String(buffer, STATUS_PREFIX_LENGTH, received - STATUS_PREFIX_LENGTH, StandardCharsets.UTF_8)
    } else {
        ""
    }

    private sealed interface DownloadReady {
        class Accepted(val length: Long) : DownloadReady
        class Refused(val response: FastbootResponse) : DownloadReady
    }

    private data class Packet(val status: ResponseStatus, val payload: String)

    private class CollectedResponse(
        val status: ResponseStatus,
        val payload: String,
        private val info: List<String>,
    ) {
        fun toResponse(): FastbootResponse {
            val text = (info + payload).filter { it.isNotEmpty() }.joinToString("\n")
            return FastbootResponse(status, text)
        }
    }

    companion object {
        const val MAX_DOWNLOAD_BYTES = 0xFFFFFFFFL
        private const val DOWNLOAD_COMMAND = "download:"
        private const val SIZE_FORMAT = "%08x"
        private const val HEX_RADIX = 16
        private const val MAX_COMMAND_BYTES = 4096
        private const val MAX_RESPONSE_BYTES = 256
        private const val STATUS_PREFIX_LENGTH = 4
        private const val STREAM_BUFFER_BYTES = 256 * 1024
    }
}
