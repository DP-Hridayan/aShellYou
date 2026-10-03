package `in`.hridayan.ashell.shell.common.data.channel

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException

private const val READ_BUFFER_SIZE = 64 * 1024

/**
 * Adapts the Wi-Fi library's stream. It exposes byte streams rather than ADB payloads, so a read
 * returns whatever has arrived, which the [AdbDuplexStream] contract allows.
 */
class LibadbDuplexStream(private val stream: AdbStream) : AdbDuplexStream {

    private val input = stream.openInputStream()
    private val output = stream.openOutputStream()
    private val buffer = ByteArray(READ_BUFFER_SIZE)

    override suspend fun read(): ByteArray = runInterruptible(Dispatchers.IO) {
        val bytesRead = input.read(buffer, 0, buffer.size)
        if (bytesRead < 0) throw IOException("Stream closed by peer")
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
