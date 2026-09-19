package `in`.hridayan.ashell.shell.file_browser.data.protocol

import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException

private const val READ_BUFFER_SIZE = 64 * 1024

/**
 * Carries a `sync:` session over the Wi-Fi ADB library's stream.
 *
 * This library exposes streams rather than whole messages, so a read returns whatever has arrived
 * rather than one protocol frame. Reassembly into exact frames is the session's job, so returning
 * partial data here is correct.
 */
class LibadbSyncTransport(private val stream: AdbStream) : SyncTransport {

    private val input = stream.openInputStream()
    private val output = stream.openOutputStream()
    private val buffer = ByteArray(READ_BUFFER_SIZE)

    override suspend fun read(): ByteArray = runInterruptible(Dispatchers.IO) {
        val bytesRead = input.read(buffer, 0, buffer.size)
        if (bytesRead < 0) throw IOException("Sync stream closed by peer")
        buffer.copyOf(bytesRead)
    }

    override suspend fun write(data: ByteArray) = runInterruptible(Dispatchers.IO) {
        output.write(data)
        output.flush()
    }

    override fun close() {
        runCatching { output.close() }
        runCatching { input.close() }
        runCatching { stream.close() }
    }
}
