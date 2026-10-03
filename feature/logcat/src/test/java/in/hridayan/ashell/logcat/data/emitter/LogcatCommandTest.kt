package `in`.hridayan.ashell.logcat.data.emitter

import org.junit.Assert.assertEquals
import org.junit.Test

private const val SINCE = "10-03 10:00:01.123"

class LogcatCommandTest {

    @Test
    fun `without a start time the whole buffer is read`() {
        assertEquals("logcat -v threadtime", LogcatCommand.shellLine(null))
        assertEquals(listOf("logcat", "-v", "threadtime"), LogcatCommand.arguments(null))
    }

    @Test
    fun `a start time is quoted on the shell line`() {
        assertEquals("logcat -v threadtime -T '$SINCE'", LogcatCommand.shellLine(SINCE))
    }

    @Test
    fun `a start time is a single argument in the argument list`() {
        assertEquals(
            listOf("logcat", "-v", "threadtime", "-T", SINCE),
            LogcatCommand.arguments(SINCE),
        )
    }

    @Test
    fun `a value that is not a threadtime timestamp is ignored`() {
        listOf("", "10-03", "x'; reboot; '", "10-03 10:00:01.123; ls").forEach {
            assertEquals("logcat -v threadtime", LogcatCommand.shellLine(it))
        }
    }
}
