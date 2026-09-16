package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.util.approximateSizeBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

private val ENTRY_BYTES = entry(0).approximateSizeBytes().toLong()
private val THREE_ENTRIES = ENTRY_BYTES * 3

private fun ids(store: LogListStateStore): List<Long> = store.state.value.logs.map { it.id }

class LogListStateStoreTest {

    @Test
    fun `defaults to auto-scroll on with no logs`() {
        val store = LogListStateStore(THREE_ENTRIES)
        assertTrue(store.state.value.isAutoScrolling)
        assertTrue(store.state.value.logs.isEmpty())
    }

    @Test
    fun `pause and resume toggle the flag`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.pause()
        assertFalse(store.state.value.isAutoScrolling)
        store.resume()
        assertTrue(store.state.value.isAutoScrolling)
    }

    @Test
    fun `reset clears logs and restores auto-scroll`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.append(listOf(entry(1)))
        store.pause()
        store.reset()
        assertTrue(store.state.value.logs.isEmpty())
        assertTrue(store.state.value.isAutoScrolling)
    }

    @Test
    fun `append at the budget evicts oldest and keeps flag`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.pause()
        store.append(listOf(entry(1), entry(2), entry(3)))
        store.append(listOf(entry(4)))
        assertEquals(listOf(2L, 3L, 4L), ids(store))
        assertFalse(store.state.value.isAutoScrolling)
    }

    @Test
    fun `append below the budget accumulates`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.append(listOf(entry(1)))
        store.append(listOf(entry(2)))
        assertEquals(listOf(1L, 2L), ids(store))
    }

    @Test
    fun `replace truncates to the budget`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.replace(listOf(entry(1), entry(2), entry(3), entry(4)))
        assertEquals(listOf(2L, 3L, 4L), ids(store))
    }

    @Test
    fun `lowering the budget trims existing logs`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.append(listOf(entry(1), entry(2), entry(3)))
        store.updateLimit(ENTRY_BYTES)
        assertEquals(listOf(3L), ids(store))
    }

    @Test
    fun `raising the budget keeps logs and allows growth`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.append(listOf(entry(1), entry(2), entry(3)))
        store.updateLimit(THREE_ENTRIES * 2)
        store.append(listOf(entry(4)))
        assertEquals(listOf(1L, 2L, 3L, 4L), ids(store))
    }

    @Test
    fun `changing the budget preserves the auto-scroll flag`() {
        val store = LogListStateStore(THREE_ENTRIES)
        store.pause()
        store.updateLimit(ENTRY_BYTES)
        assertFalse(store.state.value.isAutoScrolling)
    }
}
