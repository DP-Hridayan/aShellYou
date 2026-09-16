package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SideloadProgressTrackerTest {

    private var now = 0L
    private val tracker = SideloadProgressTracker(
        fileName = "rom.zip",
        fileSize = 100L,
        blockSize = 64,
        clock = { now },
    )

    @Test
    fun `total blocks rounds up`() {
        assertEquals(2, tracker.totalBlocks)
    }

    @Test
    fun `last block is truncated to the remaining bytes`() {
        assertEquals(64, tracker.blockLength(0))
        assertEquals(36, tracker.blockLength(1))
    }

    @Test
    fun `block validity`() {
        assertTrue(tracker.isValidBlock(0))
        assertTrue(tracker.isValidBlock(1))
        assertFalse(tracker.isValidBlock(2))
        assertFalse(tracker.isValidBlock(-1))
    }

    @Test
    fun `repeated block does not advance progress but counts bytes`() {
        tracker.onBlockSent(0, 64)
        val repeated = tracker.onBlockSent(0, 64)

        assertEquals(1, repeated.servedBlocks)
        assertEquals(0.5f, repeated.progress)
        assertEquals(128L, repeated.bytesSent)
        assertEquals(SideloadStatus.SENDING, repeated.status)
    }

    @Test
    fun `progress reaches one only after every block was served`() {
        tracker.onBlockSent(1, 36)
        val operation = tracker.onBlockSent(0, 64)

        assertEquals(1f, operation.progress)
        assertEquals(2, operation.servedBlocks)
        assertEquals(1, operation.currentBlock)
    }

    @Test
    fun `complete reports full progress and totals`() {
        val operation = tracker.complete()

        assertEquals(SideloadStatus.COMPLETE, operation.status)
        assertEquals(1f, operation.progress)
        assertEquals(2, operation.currentBlock)
        assertEquals(100L, operation.totalBytes)
    }

    @Test
    fun `failure carries error and detail`() {
        val operation = tracker.failure(SideloadError.BLOCK_OUT_OF_RANGE, "9")

        assertEquals(SideloadStatus.ERROR, operation.status)
        assertEquals(SideloadError.BLOCK_OUT_OF_RANGE, operation.error)
        assertEquals("9", operation.errorDetail)
    }

    @Test
    fun `transfer rate is megabytes per second`() {
        val large = SideloadProgressTracker("rom.zip", 4_194_304L, 1_048_576, clock = { now })
        now = 2_000L
        val operation = large.onBlockSent(0, 1_048_576)

        assertEquals(0.5f, operation.transferRateMBps)
    }
}
