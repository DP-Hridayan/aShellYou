package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus

/**
 * Turns block-level events into [SideloadOperation] snapshots. Recovery requests the same block
 * more than once, so progress is measured in distinct blocks served while the byte counter feeds
 * the transfer rate.
 */
class SideloadProgressTracker(
    private val fileName: String,
    private val fileSize: Long,
    private val blockSize: Int,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    val totalBlocks: Int = ((fileSize + blockSize - 1) / blockSize).toInt()

    private val served = BooleanArray(totalBlocks)
    private var servedCount = 0
    private var bytesSent = 0L
    private var lastBlock = 0
    private val startedAt = clock()

    fun isValidBlock(index: Int): Boolean = index in 0 until totalBlocks

    fun blockLength(index: Int): Int {
        val remaining = fileSize - index.toLong() * blockSize
        return minOf(blockSize.toLong(), remaining).toInt()
    }

    fun onBlockSent(index: Int, bytes: Int): SideloadOperation {
        if (!served[index]) {
            served[index] = true
            servedCount++
        }
        bytesSent += bytes
        lastBlock = index + 1
        return snapshot(SideloadStatus.SENDING)
    }

    fun waitingForRecovery(): SideloadOperation = snapshot(SideloadStatus.WAITING_FOR_RECOVERY)

    fun complete(): SideloadOperation = snapshot(SideloadStatus.COMPLETE).copy(
        progress = 1f,
        currentBlock = totalBlocks,
        servedBlocks = totalBlocks,
    )

    fun failure(error: SideloadError, detail: String? = null): SideloadOperation =
        snapshot(SideloadStatus.ERROR).copy(error = error, errorDetail = detail)

    private fun snapshot(status: SideloadStatus): SideloadOperation = SideloadOperation(
        fileName = fileName,
        status = status,
        progress = if (totalBlocks == 0) 0f else servedCount.toFloat() / totalBlocks,
        bytesSent = bytesSent,
        totalBytes = fileSize,
        transferRateMBps = transferRateMBps(),
        currentBlock = lastBlock,
        servedBlocks = servedCount,
        totalBlocks = totalBlocks,
    )

    private fun transferRateMBps(): Float {
        val elapsedMs = (clock() - startedAt).coerceAtLeast(1L)
        val megabytes = bytesSent.toFloat() / BYTES_PER_MEGABYTE
        return megabytes / (elapsedMs / MILLIS_PER_SECOND)
    }

    private companion object {
        const val BYTES_PER_MEGABYTE = 1_048_576f
        const val MILLIS_PER_SECOND = 1_000f
    }
}
