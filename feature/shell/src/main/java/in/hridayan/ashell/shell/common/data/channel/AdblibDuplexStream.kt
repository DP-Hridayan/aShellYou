package `in`.hridayan.ashell.shell.common.data.channel

import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * Adapts the OTG library's blocking stream. Cancelling the caller interrupts the blocked read or
 * write instead of leaving a thread parked on a stream nobody reads.
 */
class AdblibDuplexStream(
    private val stream: AdbStream,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AdbDuplexStream {

    override suspend fun read(): ByteArray = runInterruptible(ioDispatcher) { stream.read() }

    override suspend fun write(data: ByteArray) = runInterruptible(ioDispatcher) {
        stream.write(data)
    }

    override fun close() {
        runCatching { stream.close() }
    }
}
