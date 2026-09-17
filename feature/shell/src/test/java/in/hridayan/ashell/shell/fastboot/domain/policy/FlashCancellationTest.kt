package `in`.hridayan.ashell.shell.fastboot.domain.policy

import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashCancellationTest {

    @Test
    fun `reading the image can be abandoned without asking`() {
        assertEquals(FlashCancelPolicy.IMMEDIATE, FlashCancellation.policyFor(FlashStatus.READING_FILE))
    }

    @Test
    fun `abandoning a download is confirmed first because it ends the session`() {
        assertEquals(FlashCancelPolicy.CONFIRM, FlashCancellation.policyFor(FlashStatus.DOWNLOADING))
    }

    @Test
    fun `a partition write cannot be cancelled`() {
        assertEquals(FlashCancelPolicy.UNSAFE, FlashCancellation.policyFor(FlashStatus.WRITING))
        assertEquals(FlashCancelPolicy.UNSAFE, FlashCancellation.policyFor(FlashStatus.ERASING))
        assertFalse(FlashCancellation.isCancellable(FlashStatus.WRITING))
        assertFalse(FlashCancellation.isCancellable(FlashStatus.ERASING))
    }

    @Test
    fun `an operation already being cancelled is not offered again`() {
        assertFalse(FlashCancellation.isCancellable(FlashStatus.CANCELLING))
    }

    @Test
    fun `a finished operation is not blocked`() {
        assertTrue(FlashCancellation.isCancellable(FlashStatus.COMPLETE))
        assertTrue(FlashCancellation.isCancellable(FlashStatus.ERROR))
        assertTrue(FlashCancellation.isCancellable(FlashStatus.IDLE))
    }
}
