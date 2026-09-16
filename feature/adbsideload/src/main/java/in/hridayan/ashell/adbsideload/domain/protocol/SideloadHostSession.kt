package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import java.io.IOException

/**
 * Serves the `sideload-host` protocol: answers every block request from recovery with the
 * matching slice of the package until recovery reports success or failure.
 *
 * Only the first request is bounded by a timeout. Later gaps between requests are legitimate,
 * because recovery pauses reading while it verifies and flashes, so the session waits until the
 * stream closes, the user cancels, or recovery finishes.
 */
class SideloadHostSession(
    private val transport: SideloadTransport,
    private val blockReader: SideloadBlockReader,
    private val tracker: SideloadProgressTracker,
    private val blockSize: Int,
    private val firstRequestTimeoutMs: Long = FIRST_REQUEST_TIMEOUT_MS,
) {
    private val parser = SideloadRequestParser()

    fun run(): Flow<SideloadOperation> = flow {
        emit(tracker.waitingForRecovery())
        var outcome: SideloadOperation? = null
        var awaitingFirstRequest = true
        while (outcome == null) {
            outcome = when (val packet = readPacket(awaitingFirstRequest)) {
                is PacketResult.Data -> serveAll(parser.feed(packet.bytes))
                is PacketResult.Failure -> tracker.failure(packet.error, packet.detail)
            }
            awaitingFirstRequest = false
        }
        emit(outcome)
    }

    private suspend fun readPacket(bounded: Boolean): PacketResult = try {
        val bytes = if (bounded) withTimeout(firstRequestTimeoutMs) { transport.read() } else transport.read()
        PacketResult.Data(bytes)
    } catch (e: TimeoutCancellationException) {
        PacketResult.Failure(SideloadError.RECOVERY_NOT_RESPONDING, e.message)
    } catch (e: IOException) {
        PacketResult.Failure(SideloadError.STREAM_CLOSED, e.message)
    }

    private suspend fun FlowCollector<SideloadOperation>.serveAll(
        requests: List<SideloadRequest>,
    ): SideloadOperation? {
        var outcome: SideloadOperation? = null
        val iterator = requests.iterator()
        while (outcome == null && iterator.hasNext()) {
            outcome = serve(iterator.next())
        }
        return outcome
    }

    private suspend fun FlowCollector<SideloadOperation>.serve(request: SideloadRequest): SideloadOperation? =
        when (request) {
            is SideloadRequest.Block -> serveBlock(request.index)
            SideloadRequest.Done -> tracker.complete()
            SideloadRequest.Failed -> tracker.failure(SideloadError.RECOVERY_REPORTED_FAILURE)
            is SideloadRequest.Malformed -> tracker.failure(SideloadError.INVALID_REQUEST, request.frame)
        }

    private suspend fun FlowCollector<SideloadOperation>.serveBlock(index: Int): SideloadOperation? {
        if (!tracker.isValidBlock(index)) {
            return tracker.failure(SideloadError.BLOCK_OUT_OF_RANGE, index.toString())
        }
        val data = readBlock(index) ?: return tracker.failure(SideloadError.FILE_READ_FAILED, index.toString())
        return try {
            writeChunked(data)
            emit(tracker.onBlockSent(index, data.size))
            null
        } catch (e: IOException) {
            tracker.failure(SideloadError.STREAM_CLOSED, e.message)
        }
    }

    private fun readBlock(index: Int): ByteArray? {
        val length = tracker.blockLength(index)
        val data = try {
            blockReader.readBlock(index.toLong() * blockSize, length)
        } catch (_: IOException) {
            return null
        }
        return data.takeIf { it.size == length }
    }

    private suspend fun writeChunked(data: ByteArray) {
        val chunkSize = transport.maxWriteSize.takeIf { it > 0 } ?: FALLBACK_CHUNK_SIZE
        var offset = 0
        while (offset < data.size) {
            val end = minOf(offset + chunkSize, data.size)
            transport.write(data.copyOfRange(offset, end))
            offset = end
        }
    }

    private sealed interface PacketResult {
        class Data(val bytes: ByteArray) : PacketResult
        class Failure(val error: SideloadError, val detail: String?) : PacketResult
    }

    companion object {
        const val DEFAULT_BLOCK_SIZE = 65_536
        private const val FIRST_REQUEST_TIMEOUT_MS = 30_000L
        private const val FALLBACK_CHUNK_SIZE = 4_096
    }
}
