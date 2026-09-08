package `in`.hridayan.ashell.logcat.domain.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val WINDOW_MS = 50L
private const val ITEM_COUNT = 200
private const val DELAY_CYCLE_MS = 7L

@OptIn(ExperimentalCoroutinesApi::class)
class FlowBatchingTest {

    @Test
    fun `single item is emitted after exactly one window`() = runTest {
        val collected = mutableListOf<List<Int>>()
        val job = launch {
            flow {
                emit(1)
                delay(Long.MAX_VALUE)
            }.batchByTime(WINDOW_MS).collect { collected += it }
        }
        advanceTimeBy(WINDOW_MS - 1)
        assertTrue(collected.isEmpty())
        advanceTimeBy(2)
        assertEquals(listOf(listOf(1)), collected)
        job.cancel()
    }

    @Test
    fun `items inside one window form a single ordered batch`() = runTest {
        val result = flow {
            emit(1)
            emit(2)
            emit(3)
        }.batchByTime(WINDOW_MS).toList()
        assertEquals(listOf(listOf(1, 2, 3)), result)
    }

    @Test
    fun `items across windows form separate batches`() = runTest {
        val result = flow {
            emit(1)
            delay(WINDOW_MS * 2)
            emit(2)
        }.batchByTime(WINDOW_MS).toList()
        assertEquals(listOf(listOf(1), listOf(2)), result)
    }

    @Test
    fun `upstream completion flushes the pending tail`() = runTest {
        val result = flow { emit(1) }.batchByTime(WINDOW_MS).toList()
        assertEquals(listOf(listOf(1)), result)
    }

    @Test
    fun `no element is lost across window boundaries`() = runTest {
        val input = (1..ITEM_COUNT).toList()
        val result = flow {
            input.forEach {
                emit(it)
                delay(it % DELAY_CYCLE_MS)
            }
        }.batchByTime(WINDOW_MS).toList()
        assertEquals(input, result.flatten())
    }
}
