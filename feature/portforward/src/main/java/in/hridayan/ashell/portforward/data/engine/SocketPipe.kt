package `in`.hridayan.ashell.portforward.data.engine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

/**
 * Copies bytes both ways between a client and its target until either side ends.
 *
 * When one direction ends, both endpoints are closed. Device streams have no half-close, so a
 * relayed connection ends as a whole, and closing is also what unblocks the other direction's read.
 */
class SocketPipe(
    private val dispatcher: CoroutineDispatcher,
    private val bufferBytes: Int = DEFAULT_BUFFER_BYTES
) {

    /** Bytes that crossed one connection. */
    data class Totals(val fromClient: Long, val toClient: Long)

    /**
     * @param sentCounter and [receivedCounter] are increased as bytes cross, so the caller can
     * report live totals across many connections.
     */
    suspend fun run(
        client: PipeEndpoint,
        target: PipeEndpoint,
        sentCounter: AtomicLong = AtomicLong(),
        receivedCounter: AtomicLong = AtomicLong()
    ): Totals {
        val fromClient = AtomicLong()
        val toClient = AtomicLong()
        coroutineScope {
            try {
                val upstream = async(dispatcher) { copy(client, target, fromClient, sentCounter) }
                val downstream = async(dispatcher) { copy(target, client, toClient, receivedCounter) }
                select {
                    upstream.onAwait {}
                    downstream.onAwait {}
                }
            } finally {
                client.closeQuietly()
                target.closeQuietly()
            }
        }
        return Totals(fromClient = fromClient.get(), toClient = toClient.get())
    }

    private suspend fun copy(
        from: PipeEndpoint,
        to: PipeEndpoint,
        connectionCounter: AtomicLong,
        tunnelCounter: AtomicLong
    ) {
        val buffer = ByteArray(bufferBytes)
        try {
            while (true) {
                val count = from.read(buffer)
                if (count < 0) break
                to.write(buffer, 0, count)
                connectionCounter.addAndGet(count.toLong())
                tunnelCounter.addAndGet(count.toLong())
            }
        } catch (_: IOException) {
        }
    }

    private companion object {
        const val DEFAULT_BUFFER_BYTES = 64 * 1024
    }
}
