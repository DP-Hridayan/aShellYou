package `in`.hridayan.ashell.shell.file_browser.domain.protocol

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

private const val DEFAULT_FILE_MODE = 420
private const val MILLIS_PER_SECOND = 1000L

/**
 * Drives one ADB `sync:` session over a [SyncTransport].
 *
 * The session is strictly request and response on a single stream, so one instance carries one
 * transfer at a time. Concurrent use would interleave frames and corrupt the protocol.
 */
class SyncSession(private val transport: SyncTransport) {

    private val reader = SyncFrameReader(transport)

    suspend fun stat(path: String): SyncStat {
        transport.write(SyncProtocol.request(SyncProtocol.ID_STAT, path))
        val frame = reader.readFrame()
        if (frame.id != SyncProtocol.ID_STAT) {
            throw SyncProtocolException("Expected a stat reply but got '${frame.id}'")
        }
        val payload = reader.readExactly(SyncProtocol.STAT_PAYLOAD_SIZE)
        return SyncProtocol.decodeStat(payload)
    }

    /**
     * Downloads [path] into [sink]. [onProgress] receives the running total of bytes written.
     */
    suspend fun pull(path: String, sink: OutputStream, onProgress: suspend (Long) -> Unit) {
        transport.write(SyncProtocol.request(SyncProtocol.ID_RECV, path))

        var received = 0L
        while (true) {
            val frame = reader.readFrame()
            when (frame.id) {
                SyncProtocol.ID_DATA -> {
                    val chunk = reader.readExactly(frame.value)
                    sink.write(chunk)
                    received += chunk.size
                    onProgress(received)
                }

                SyncProtocol.ID_DONE -> {
                    sink.flush()
                    return
                }

                SyncProtocol.ID_FAIL -> throw failure(frame.value)

                else -> throw SyncProtocolException("Unexpected message '${frame.id}' during download")
            }
        }
    }

    /**
     * Uploads [source] to [path]. [onProgress] receives the running total of bytes sent.
     *
     * The device acknowledges the whole transfer, so a refused or truncated write is reported rather
     * than passing silently.
     */
    suspend fun push(
        source: InputStream,
        path: String,
        mode: Int = DEFAULT_FILE_MODE,
        onProgress: suspend (Long) -> Unit
    ) {
        transport.write(SyncProtocol.request(SyncProtocol.ID_SEND, "$path,$mode"))

        var sent = 0L
        val buffer = ByteArray(SyncProtocol.MAX_DATA_CHUNK)
        while (true) {
            val read = source.read(buffer)
            if (read < 0) break
            if (read == 0) continue

            transport.write(SyncProtocol.header(SyncProtocol.ID_DATA, read))
            transport.write(buffer.copyOf(read))
            sent += read
            onProgress(sent)
        }

        val modifiedAtSeconds = (System.currentTimeMillis() / MILLIS_PER_SECOND).toInt()
        transport.write(SyncProtocol.header(SyncProtocol.ID_DONE, modifiedAtSeconds))

        val frame = reader.readFrame()
        when (frame.id) {
            SyncProtocol.ID_OKAY -> return
            SyncProtocol.ID_FAIL -> throw failure(frame.value)
            else -> throw SyncProtocolException("Unexpected message '${frame.id}' after upload")
        }
    }

    suspend fun quit() {
        transport.write(SyncProtocol.header(SyncProtocol.ID_QUIT, 0))
    }

    private suspend fun failure(length: Int): SyncProtocolException {
        val message = String(reader.readExactly(length), StandardCharsets.UTF_8)
        return SyncProtocolException(message)
    }
}

/**
 * Reassembles exact byte counts from a transport that delivers arbitrary chunks.
 *
 * A frame header or payload can arrive split across several reads, or several frames can arrive in
 * one, so the session cannot read directly from the transport.
 */
internal class SyncFrameReader(private val transport: SyncTransport) {

    private var pending = ByteArray(0)
    private var offset = 0

    suspend fun readFrame(): SyncFrame =
        SyncProtocol.decodeHeader(readExactly(SyncProtocol.HEADER_SIZE))

    suspend fun readExactly(count: Int): ByteArray {
        if (count == 0) return ByteArray(0)

        val result = ByteArray(count)
        var filled = 0
        while (filled < count) {
            if (offset >= pending.size) refill()
            val take = minOf(count - filled, pending.size - offset)
            pending.copyInto(result, filled, offset, offset + take)
            offset += take
            filled += take
        }
        return result
    }

    private suspend fun refill() {
        while (true) {
            val chunk = transport.read()
            if (chunk.isNotEmpty()) {
                pending = chunk
                offset = 0
                return
            }
        }
    }
}
