package `in`.hridayan.ashell.core.common.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLoggerTest {

    @Test
    fun `debug logging policy allows debug logs only in debuggable builds`() {
        assertTrue(LogLevelPolicy.shouldLogDebug(isDebuggable = true))
        assertFalse(LogLevelPolicy.shouldLogDebug(isDebuggable = false))
    }

    @Test
    fun `NoOpAppLogger handles all log levels safely on jvm`() {
        val logger: AppLogger = NoOpAppLogger

        logger.d("Tag", "debug message")
        logger.w("Tag", "warning message")
        logger.w("Tag", "warning with cause", IllegalStateException("boom"))
        logger.e("Tag", "error message")
        logger.e("Tag", "error with cause", RuntimeException("crash"))
    }
}
