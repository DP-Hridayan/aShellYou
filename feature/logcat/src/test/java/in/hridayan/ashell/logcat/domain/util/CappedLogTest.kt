package `in`.hridayan.ashell.logcat.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

private const val ITEM_BYTES = 10
private const val BUDGET_BYTES = 30L

private val sizeOf: (Int) -> Int = { ITEM_BYTES }

private fun logOf(vararg items: Int): CappedLog<Int> =
    cappedLogOf(items.toList(), BUDGET_BYTES, sizeOf)

class CappedLogTest {

    @Test
    fun `under budget concatenates`() {
        val result = logOf(1, 2).append(listOf(3), BUDGET_BYTES, sizeOf)
        assertEquals(listOf(1, 2, 3), result.items)
        assertEquals(3 * ITEM_BYTES.toLong(), result.bytes)
    }

    @Test
    fun `over budget evicts the oldest entries`() {
        val result = logOf(1, 2, 3).append(listOf(4, 5), BUDGET_BYTES, sizeOf)
        assertEquals(listOf(3, 4, 5), result.items)
        assertEquals(BUDGET_BYTES, result.bytes)
    }

    @Test
    fun `batch larger than the budget keeps only what fits`() {
        val result = logOf(1, 2).append(listOf(3, 4, 5, 6), BUDGET_BYTES, sizeOf)
        assertEquals(listOf(4, 5, 6), result.items)
    }

    @Test
    fun `empty batch is identity`() {
        val result = logOf(1, 2, 3).append(emptyList(), BUDGET_BYTES, sizeOf)
        assertEquals(listOf(1, 2, 3), result.items)
    }

    @Test
    fun `newest entry survives a budget smaller than one entry`() {
        val result = logOf(1, 2).append(listOf(3), maxBytes = 1L, sizeOf = sizeOf)
        assertEquals(listOf(3), result.items)
    }

    @Test
    fun `building from a list keeps the newest entries`() {
        val result = cappedLogOf(listOf(1, 2, 3, 4, 5), BUDGET_BYTES, sizeOf)
        assertEquals(listOf(3, 4, 5), result.items)
        assertEquals(BUDGET_BYTES, result.bytes)
    }

    @Test
    fun `lowering the budget trims the oldest entries`() {
        val result = logOf(1, 2, 3).trimmedTo(maxBytes = ITEM_BYTES.toLong(), sizeOf = sizeOf)
        assertEquals(listOf(3), result.items)
        assertEquals(ITEM_BYTES.toLong(), result.bytes)
    }

    @Test
    fun `raising the budget keeps everything`() {
        val result = logOf(1, 2, 3).trimmedTo(maxBytes = BUDGET_BYTES * 2, sizeOf = sizeOf)
        assertEquals(listOf(1, 2, 3), result.items)
    }

    @Test
    fun `tracked size stays consistent across many appends`() {
        var log = CappedLog<Int>()
        repeat(100) { log = log.append(listOf(it), BUDGET_BYTES, sizeOf) }
        assertEquals(log.items.size * ITEM_BYTES.toLong(), log.bytes)
        assertEquals(listOf(97, 98, 99), log.items)
    }
}
