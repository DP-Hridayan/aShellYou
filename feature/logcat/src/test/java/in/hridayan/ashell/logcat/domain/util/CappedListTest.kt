package `in`.hridayan.ashell.logcat.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CappedListTest {

    @Test
    fun `under cap concatenates`() {
        val result = listOf(1, 2).appendCapped(listOf(3, 4), max = 10)
        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun `at cap evicts oldest by batch size`() {
        val result = listOf(1, 2, 3).appendCapped(listOf(4, 5), max = 3)
        assertEquals(listOf(3, 4, 5), result)
    }

    @Test
    fun `batch larger than cap keeps last max of batch only`() {
        val result = listOf(1, 2).appendCapped(listOf(3, 4, 5, 6), max = 3)
        assertEquals(listOf(4, 5, 6), result)
    }

    @Test
    fun `empty batch is identity`() {
        val result = listOf(1, 2, 3).appendCapped(emptyList(), max = 3)
        assertEquals(listOf(1, 2, 3), result)
    }
}
