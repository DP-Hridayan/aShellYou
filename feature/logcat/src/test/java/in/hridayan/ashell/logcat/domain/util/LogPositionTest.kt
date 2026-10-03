package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import org.junit.Assert.assertEquals
import org.junit.Test

private fun entry(id: Long) = LogEntry(
    id = id,
    timestamp = "",
    pid = "",
    tid = "",
    uid = "",
    packageName = "",
    level = LogLevel.INFO,
    tag = "tag",
    message = "message",
)

private fun logsOf(vararg ids: Long): List<LogEntry> = ids.map { entry(it) }

class LogPositionTest {

    @Test
    fun `a retained entry is found at its index`() {
        assertEquals(2, logsOf(10, 20, 30, 40).indexAtOrAfter(30))
    }

    @Test
    fun `an evicted entry resolves to the first newer entry`() {
        assertEquals(0, logsOf(30, 40, 50).indexAtOrAfter(10))
    }

    @Test
    fun `an id missing from a filtered list resolves to the next newer entry`() {
        assertEquals(2, logsOf(10, 20, 40, 50).indexAtOrAfter(30))
    }

    @Test
    fun `an id newer than every entry resolves past the end`() {
        assertEquals(3, logsOf(10, 20, 30).indexAtOrAfter(99))
    }

    @Test
    fun `an empty list resolves to zero`() {
        assertEquals(0, emptyList<LogEntry>().indexAtOrAfter(5))
    }

    @Test
    fun `eviction from the head shifts the index by the evicted count`() {
        val before = logsOf(1, 2, 3, 4, 5, 6)
        val after = logsOf(4, 5, 6, 7, 8)
        assertEquals(4, before.indexAtOrAfter(5))
        assertEquals(1, after.indexAtOrAfter(5))
    }
}
