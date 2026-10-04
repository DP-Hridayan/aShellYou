package `in`.hridayan.ashell.logcat.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppIconStackTest {

    @Test
    fun `no apps draw nothing`() {
        assertEquals(AppIconStack(emptyList(), null, 0), appIconStack(emptyList()))
    }

    @Test
    fun `three apps are all shown with no counter`() {
        val stack = appIconStack(listOf("a", "b", "c"))
        assertEquals(listOf("a", "b", "c"), stack.shown)
        assertNull(stack.overflowPackage)
        assertEquals(0, stack.overflowCount)
    }

    @Test
    fun `a fourth app is dimmed under a counter of one`() {
        val stack = appIconStack(listOf("a", "b", "c", "d"))
        assertEquals(listOf("a", "b", "c"), stack.shown)
        assertEquals("d", stack.overflowPackage)
        assertEquals(1, stack.overflowCount)
    }

    @Test
    fun `the counter covers every app beyond the first three`() {
        assertEquals(2, appIconStack(listOf("a", "b", "c", "d", "e")).overflowCount)
    }

    @Test
    fun `the counter stops at ninety nine`() {
        val packages = (1..150).map { "app$it" }
        assertEquals(99, appIconStack(packages).overflowCount)
        assertEquals("app4", appIconStack(packages).overflowPackage)
    }

    @Test
    fun `apps keep the order they were entered in`() {
        assertEquals(listOf("z", "a", "m"), appIconStack(linkedSetOf("z", "a", "m", "b")).shown)
    }
}
