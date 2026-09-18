package `in`.hridayan.ashell.shell.file_browser.data.protocol

import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException

/**
 * Carries a `sync:` session over the OTG ADB library's stream.
 *
 * The underlying read blocks until data arrives or the stream closes. Running it through
 * [runInterruptible] means cancelling a transfer interrupts that call rather than leaving a thread
 * parked on it.
 */
class AdblibSyncTransport(private val stream: AdbStream) : SyncTransport {

    override suspend fun read(): ByteArray = runInterruptible(Dispatchers.IO) {
        stream.read() ?: throw IOException("Sync stream closed by peer")
    }

    override suspend fun write(data: ByteArray) = runInterruptible(Dispatchers.IO) {
        stream.write(data)
    }

    override fun close() {
        runCatching { stream.close() }
    }
}
