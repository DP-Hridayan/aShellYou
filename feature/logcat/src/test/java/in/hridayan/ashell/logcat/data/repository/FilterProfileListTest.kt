package `in`.hridayan.ashell.logcat.data.repository

import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import org.junit.Assert.assertEquals
import org.junit.Test

private fun profile(id: String, name: String = id) = LogFilter(id = id, name = name)

class FilterProfileListTest {

    @Test
    fun `a new profile is appended`() {
        val result = listOf(profile("a"), profile("b")).upsert(profile("c"))
        assertEquals(listOf("a", "b", "c"), result.map { it.id })
    }

    @Test
    fun `an edited profile keeps its position`() {
        val result = listOf(profile("a"), profile("b"), profile("c")).upsert(profile("a", "renamed"))
        assertEquals(listOf("a", "b", "c"), result.map { it.id })
        assertEquals("renamed", result.first().name)
    }

    @Test
    fun `toggling adds an inactive id and removes an active one`() {
        assertEquals(setOf("a", "b"), setOf("a").toggled("b"))
        assertEquals(setOf("a"), setOf("a", "b").toggled("b"))
    }
}
