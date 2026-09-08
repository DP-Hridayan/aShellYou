package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MAX = 3

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

private fun ids(store: LogListStateStore): List<Long> = store.state.value.logs.map { it.id }

class LogListStateStoreTest {

    @Test
    fun `defaults to auto-scroll on with no logs`() {
        val store = LogListStateStore(MAX)
        assertTrue(store.state.value.isAutoScrolling)
        assertTrue(store.state.value.logs.isEmpty())
    }

    @Test
    fun `pause and resume toggle the flag`() {
        val store = LogListStateStore(MAX)
        store.pause()
        assertFalse(store.state.value.isAutoScrolling)
        store.resume()
        assertTrue(store.state.value.isAutoScrolling)
    }

    @Test
    fun `reset clears logs and restores auto-scroll`() {
        val store = LogListStateStore(MAX)
        store.append(listOf(entry(1)))
        store.pause()
        store.reset()
        assertTrue(store.state.value.logs.isEmpty())
        assertTrue(store.state.value.isAutoScrolling)
    }

    @Test
    fun `append at cap evicts oldest and keeps flag`() {
        val store = LogListStateStore(MAX)
        store.pause()
        store.append(listOf(entry(1), entry(2), entry(3)))
        store.append(listOf(entry(4)))
        assertEquals(listOf(2L, 3L, 4L), ids(store))
        assertFalse(store.state.value.isAutoScrolling)
    }

    @Test
    fun `replace truncates to max logs`() {
        val store = LogListStateStore(MAX)
        store.replace(listOf(entry(1), entry(2), entry(3), entry(4)))
        assertEquals(listOf(2L, 3L, 4L), ids(store))
    }
}
