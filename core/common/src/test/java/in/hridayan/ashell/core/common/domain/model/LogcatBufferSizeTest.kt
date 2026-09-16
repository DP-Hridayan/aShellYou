package `in`.hridayan.ashell.core.common.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val LEGACY_ENTRY_COUNT_CAP = 2000
private const val BYTES_PER_MEGABYTE = 1024L * 1024L

class LogcatBufferSizeTest {

    @Test
    fun `every offered option sanitizes to itself`() {
        LogcatBufferSize.ALL.forEach { option ->
            assertEquals(option, LogcatBufferSize.sanitize(option))
        }
    }

    @Test
    fun `default is an offered option`() {
        assertTrue(LogcatBufferSize.DEFAULT in LogcatBufferSize.ALL)
    }

    @Test
    fun `unknown values fall back to the default`() {
        listOf(0, -1, 1, 3, 15, LEGACY_ENTRY_COUNT_CAP, Int.MAX_VALUE, Int.MIN_VALUE)
            .forEach { assertEquals(LogcatBufferSize.DEFAULT, LogcatBufferSize.sanitize(it)) }
    }

    @Test
    fun `smallest option is the lower bound of every option`() {
        assertEquals(LogcatBufferSize.SMALL, LogcatBufferSize.ALL.min())
    }

    @Test
    fun `toBytes converts megabytes`() {
        assertEquals(
            LogcatBufferSize.SMALL * BYTES_PER_MEGABYTE,
            LogcatBufferSize.toBytes(LogcatBufferSize.SMALL)
        )
    }

    @Test
    fun `toBytes sanitizes before converting`() {
        assertEquals(
            LogcatBufferSize.DEFAULT * BYTES_PER_MEGABYTE,
            LogcatBufferSize.toBytes(LEGACY_ENTRY_COUNT_CAP)
        )
    }
}
