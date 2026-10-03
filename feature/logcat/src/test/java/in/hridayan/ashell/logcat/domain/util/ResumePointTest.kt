package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val EARLIER = "10-03 10:00:00.000"
private const val BOUNDARY = "10-03 10:00:01.000"
private const val LATER = "10-03 10:00:02.000"

private fun entry(id: Long, timestamp: String, message: String = "message") = LogEntry(
    id = id,
    timestamp = timestamp,
    pid = "1",
    tid = "1",
    uid = "",
    packageName = "",
    level = LogLevel.INFO,
    tag = "tag",
    message = message,
)

class ResumePointTrackerTest {

    @Test
    fun `nothing recorded means no resume point`() {
        assertNull(ResumePointTracker().current())
    }

    @Test
    fun `tracks the newest timestamp and every entry stamped with it`() {
        val tracker = ResumePointTracker()
        tracker.record(entry(1, EARLIER))
        tracker.record(entry(2, BOUNDARY, "a"))
        tracker.record(entry(3, BOUNDARY, "b"))
        val point = tracker.current()
        assertEquals(BOUNDARY, point?.timestamp)
        assertEquals(listOf(2L, 3L), point?.heldAtTimestamp?.map { it.id })
    }

    @Test
    fun `a newer timestamp replaces the held boundary entries`() {
        val tracker = ResumePointTracker()
        tracker.record(entry(1, BOUNDARY))
        tracker.record(entry(2, LATER))
        assertEquals(listOf(2L), tracker.current()?.heldAtTimestamp?.map { it.id })
    }

    @Test
    fun `reset forgets the resume point`() {
        val tracker = ResumePointTracker()
        tracker.record(entry(1, BOUNDARY))
        tracker.reset()
        assertNull(tracker.current())
    }
}

class ResumeOverlapTest {

    private val held = listOf(entry(1, BOUNDARY, "a"), entry(2, BOUNDARY, "b"))
    private val point = ResumePoint(BOUNDARY, held)

    @Test
    fun `without a resume point every entry passes`() = runTest {
        val input = listOf(entry(10, BOUNDARY, "a"), entry(11, LATER))
        assertEquals(input, flowOf(*input.toTypedArray()).dropResumeOverlap(null).toList())
    }

    @Test
    fun `re-sent boundary entries are dropped even with new ids`() = runTest {
        val resent = flowOf(entry(10, BOUNDARY, "a"), entry(11, BOUNDARY, "b"), entry(12, LATER))
        val result = resent.dropResumeOverlap(point).toList()
        assertEquals(listOf(12L), result.map { it.id })
    }

    @Test
    fun `a new entry at the boundary timestamp is kept`() = runTest {
        val resent = flowOf(entry(10, BOUNDARY, "a"), entry(11, BOUNDARY, "b"), entry(12, BOUNDARY, "c"))
        val result = resent.dropResumeOverlap(point).toList()
        assertEquals(listOf("c"), result.map { it.message })
    }

    @Test
    fun `an identical line logged twice keeps the second copy`() = runTest {
        val onlyA = ResumePoint(BOUNDARY, listOf(entry(1, BOUNDARY, "a")))
        val resent = flowOf(entry(10, BOUNDARY, "a"), entry(11, BOUNDARY, "a"))
        val result = resent.dropResumeOverlap(onlyA).toList()
        assertEquals(listOf(11L), result.map { it.id })
    }

    @Test
    fun `matching stops once the stream moves past the boundary`() = runTest {
        val resent = flowOf(entry(10, LATER, "x"), entry(11, BOUNDARY, "a"))
        val result = resent.dropResumeOverlap(point).toList()
        assertEquals(listOf(10L, 11L), result.map { it.id })
    }
}
