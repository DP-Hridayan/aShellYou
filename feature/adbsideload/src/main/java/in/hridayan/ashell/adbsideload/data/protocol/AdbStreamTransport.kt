package `in`.hridayan.ashell.adbsideload.data.protocol

import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * Adapts a blocking [AdbStream] to the suspending [SideloadTransport]. Coroutine cancellation
 * interrupts the blocking call, so a cancelled sideload never leaves a thread parked on the stream.
 */
class AdbStreamTransport(
    private val stream: AdbStream,
    override val maxWriteSize: Int,
) : SideloadTransport {

    override suspend fun read(): ByteArray = runInterruptible(Dispatchers.IO) { stream.read() }

    override suspend fun write(data: ByteArray) = runInterruptible(Dispatchers.IO) { stream.write(data) }

    override fun close() {
        runCatching { stream.close() }
    }
}
