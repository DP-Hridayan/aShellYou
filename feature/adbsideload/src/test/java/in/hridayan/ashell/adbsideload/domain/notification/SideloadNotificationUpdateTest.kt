package `in`.hridayan.ashell.adbsideload.domain.notification

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SideloadNotificationUpdateTest {

    @Test
    fun `preparing the file has no measurable progress`() {
        val update = operation(SideloadStatus.READING_FILE, progress = 0f).toNotificationUpdate()

        assertTrue(update.isIndeterminate)
    }

    @Test
    fun `waiting for recovery has no measurable progress`() {
        val update = operation(SideloadStatus.WAITING_FOR_RECOVERY, progress = 0f).toNotificationUpdate()

        assertTrue(update.isIndeterminate)
    }

    @Test
    fun `sending reports a percentage`() {
        val update = operation(SideloadStatus.SENDING, progress = 0.42f).toNotificationUpdate()

        assertFalse(update.isIndeterminate)
        assertEquals(42, update.percent)
    }

    @Test
    fun `completion reports a full bar`() {
        val update = operation(SideloadStatus.COMPLETE, progress = 1f).toNotificationUpdate()

        assertEquals(100, update.percent)
    }

    @Test
    fun `progress outside the expected range is clamped`() {
        assertEquals(100, operation(SideloadStatus.SENDING, progress = 1.5f).toNotificationUpdate().percent)
        assertEquals(0, operation(SideloadStatus.SENDING, progress = -0.5f).toNotificationUpdate().percent)
    }

    private fun operation(status: SideloadStatus, progress: Float) =
        SideloadOperation(status = status, progress = progress)
}
