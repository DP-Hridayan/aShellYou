package `in`.hridayan.ashell.logcat.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadLogsPermissionTest {

    @Test
    fun `grant command targets the given package`() {
        val command = ReadLogsPermission.grantCommand("in.hridayan.ashell.debug")
        assertEquals("adb shell pm grant in.hridayan.ashell.debug android.permission.READ_LOGS", command)
    }
}
