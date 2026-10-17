package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/** One side of a relayed connection. Each direction is read by one coroutine at a time. */
interface PipeEndpoint : Closeable {

    /** Reads into [buffer] and returns the byte count, or -1 at end of stream. */
    suspend fun read(buffer: ByteArray): Int

    suspend fun write(buffer: ByteArray, offset: Int, length: Int)
}

/**
 * A [PipeEndpoint] over a connected TCP socket. Closing it closes the socket, which also unblocks a
 * pending read.
 */
class SocketEndpoint(
    private val socket: Socket,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PipeEndpoint {

    private val input: InputStream = socket.getInputStream()
    private val output: OutputStream = socket.getOutputStream()

    override suspend fun read(buffer: ByteArray): Int = runInterruptible(dispatcher) { input.read(buffer) }

    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) = runInterruptible(dispatcher) {
        output.write(buffer, offset, length)
        output.flush()
    }

    override fun close() = socket.close()
}

/**
 * A [PipeEndpoint] over an ADB stream to a device socket. An ADB read returns a whole payload, so
 * whatever does not fit the caller's buffer is kept for the next read.
 */
class AdbStreamEndpoint(private val stream: AdbDuplexStream) : PipeEndpoint {

    private var pending: ByteArray? = null
    private var pendingOffset = 0

    override suspend fun read(buffer: ByteArray): Int {
        val chunk = pending ?: nextPayload() ?: return END_OF_STREAM
        val count = minOf(buffer.size, chunk.size - pendingOffset)
        chunk.copyInto(buffer, destinationOffset = 0, startIndex = pendingOffset, endIndex = pendingOffset + count)
        pendingOffset += count
        if (pendingOffset == chunk.size) {
            pending = null
            pendingOffset = 0
        } else {
            pending = chunk
        }
        return count
    }

    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) =
        stream.write(buffer.copyOfRange(offset, offset + length))

    override fun close() = stream.close()

    private suspend fun nextPayload(): ByteArray? = try {
        stream.read()
    } catch (_: IOException) {
        null
    }

    private companion object {
        const val END_OF_STREAM = -1
    }
}

/** Closes this, ignoring the failure of a socket that is already broken. */
fun Closeable.closeQuietly() {
    try {
        close()
    } catch (_: IOException) {
    }
}
