package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
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

private val LOGS = listOf(1L, 2L, 4L, 7L, 9L, 12L).map(::entry)

private fun LogSelectionStore.ids(): Set<Long> = state.value.selectedIds

class LogSelectionStoreTest {

    @Test
    fun `starts inactive with nothing selected`() {
        val store = LogSelectionStore()
        assertFalse(store.state.value.isActive)
        assertTrue(store.ids().isEmpty())
    }

    @Test
    fun `starting with a row activates the mode with that row`() {
        val store = LogSelectionStore()
        store.startWith(4)
        assertTrue(store.state.value.isActive)
        assertEquals(setOf(4L), store.ids())
    }

    @Test
    fun `starting empty activates the mode with nothing selected`() {
        val store = LogSelectionStore()
        store.start()
        assertTrue(store.state.value.isActive)
        assertTrue(store.ids().isEmpty())
    }

    @Test
    fun `toggle adds and removes a row`() {
        val store = LogSelectionStore()
        store.startWith(1)
        store.toggle(4)
        assertEquals(setOf(1L, 4L), store.ids())
        store.toggle(1)
        assertEquals(setOf(4L), store.ids())
    }

    @Test
    fun `unselecting the last row ends the mode`() {
        val store = LogSelectionStore()
        store.startWith(4)
        store.toggle(4)
        assertFalse(store.state.value.isActive)
    }

    @Test
    fun `range selects every row from the anchor down to the target`() {
        val store = LogSelectionStore()
        store.startWith(2)
        store.extendTo(9, LOGS)
        assertEquals(setOf(2L, 4L, 7L, 9L), store.ids())
    }

    @Test
    fun `range selects every row from the anchor up to the target`() {
        val store = LogSelectionStore()
        store.startWith(9)
        store.extendTo(2, LOGS)
        assertEquals(setOf(2L, 4L, 7L, 9L), store.ids())
    }

    @Test
    fun `range keeps rows selected outside it`() {
        val store = LogSelectionStore()
        store.startWith(12)
        store.toggle(1)
        store.extendTo(4, LOGS)
        assertEquals(setOf(1L, 2L, 4L, 12L), store.ids())
    }

    @Test
    fun `the target of a range becomes the next anchor`() {
        val store = LogSelectionStore()
        store.startWith(1)
        store.extendTo(4, LOGS)
        assertEquals(4L, store.state.value.anchorId)
    }

    @Test
    fun `a toggled row becomes the next anchor`() {
        val store = LogSelectionStore()
        store.startWith(1)
        store.extendTo(2, LOGS)
        store.toggle(1)
        store.extendTo(7, LOGS)
        assertEquals(setOf(1L, 2L, 4L, 7L), store.ids())
    }

    @Test
    fun `range without an anchor in the list only toggles the target`() {
        val store = LogSelectionStore()
        store.start()
        store.extendTo(7, LOGS)
        assertEquals(setOf(7L), store.ids())
    }

    @Test
    fun `range from an evicted anchor only toggles the target`() {
        val store = LogSelectionStore()
        store.startWith(1)
        store.extendTo(7, LOGS.drop(2))
        assertEquals(setOf(1L, 7L), store.ids())
    }

    @Test
    fun `select all picks every row and keeps the mode`() {
        val store = LogSelectionStore()
        store.start()
        store.selectAll(LOGS)
        assertEquals(LOGS.map { it.id }.toSet(), store.ids())
        assertTrue(store.state.value.isActive)
    }

    @Test
    fun `deselect all empties the selection but keeps the mode`() {
        val store = LogSelectionStore()
        store.startWith(4)
        store.deselectAll()
        assertTrue(store.ids().isEmpty())
        assertTrue(store.state.value.isActive)
    }

    @Test
    fun `exit clears everything`() {
        val store = LogSelectionStore()
        store.startWith(4)
        store.exit()
        assertFalse(store.state.value.isActive)
        assertTrue(store.ids().isEmpty())
    }

    @Test
    fun `rows older than the oldest kept row are dropped`() {
        val store = LogSelectionStore()
        store.startWith(1)
        store.toggle(4)
        store.toggle(9)
        store.dropOlderThan(5)
        assertEquals(setOf(9L), store.ids())
        assertTrue(store.state.value.isActive)
    }

    @Test
    fun `selected entries come back in list order`() {
        val store = LogSelectionStore()
        store.startWith(9)
        store.toggle(2)
        store.toggle(12)
        assertEquals(listOf(2L, 9L, 12L), store.selectedIn(LOGS).map { it.id })
    }
}
