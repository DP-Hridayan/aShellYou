package `in`.hridayan.ashell.logcat.data.session

import `in`.hridayan.ashell.core.common.domain.model.LogcatBufferSize
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.util.approximateSizeBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val LEGACY_ENTRY_COUNT_CAP = 2000

private fun entry(id: Long) = LogEntry(
    id = id,
    timestamp = "09-15 10:00:00.000",
    pid = "1234",
    tid = "5678",
    uid = "",
    packageName = "",
    level = LogLevel.INFO,
    tag = "tag",
    message = "message",
)

private fun LogcatSessionHolder.fill(count: Int) {
    repeat(count) { appendToBuffer(entry(it.toLong())) }
}

class LogcatSessionHolderTest {

    @Test
    fun `smallest budget retains far more than the legacy cap`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LogcatBufferSize.SMALL)
        holder.fill(LEGACY_ENTRY_COUNT_CAP * 5)
        assertTrue(holder.rawBuffer.size > LEGACY_ENTRY_COUNT_CAP * 4)
    }

    @Test
    fun `buffer stays within the selected budget`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LogcatBufferSize.SMALL)
        holder.fill(LEGACY_ENTRY_COUNT_CAP * 20)
        val retained = holder.rawBuffer.sumOf { it.approximateSizeBytes().toLong() }
        assertTrue(retained <= LogcatBufferSize.toBytes(LogcatBufferSize.SMALL))
    }

    @Test
    fun `oldest entries are evicted first`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LogcatBufferSize.SMALL)
        val total = LogcatBufferSize.toBytes(LogcatBufferSize.SMALL).toInt()
        holder.fill(total)
        val buffer = holder.rawBuffer
        assertEquals(total - 1L, buffer.last().id)
        assertTrue(buffer.first().id > 0L)
    }

    @Test
    fun `lowering the budget trims an already filled buffer`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LogcatBufferSize.LARGE)
        holder.fill(LEGACY_ENTRY_COUNT_CAP * 20)
        val beforeSize = holder.rawBuffer.size
        holder.updateLimit(LogcatBufferSize.SMALL)
        val retained = holder.rawBuffer.sumOf { it.approximateSizeBytes().toLong() }
        assertTrue(holder.rawBuffer.size < beforeSize)
        assertTrue(retained <= LogcatBufferSize.toBytes(LogcatBufferSize.SMALL))
    }

    @Test
    fun `raising the budget keeps existing entries`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LogcatBufferSize.SMALL)
        holder.fill(LEGACY_ENTRY_COUNT_CAP)
        val before = holder.rawBuffer.map { it.id }
        holder.updateLimit(LogcatBufferSize.VERY_LARGE)
        assertEquals(before, holder.rawBuffer.map { it.id })
    }

    @Test
    fun `an unsafe stored budget falls back to the default`() {
        val holder = LogcatSessionHolder()
        holder.updateLimit(LEGACY_ENTRY_COUNT_CAP)
        holder.fill(LEGACY_ENTRY_COUNT_CAP * 20)
        val retained = holder.rawBuffer.sumOf { it.approximateSizeBytes().toLong() }
        assertTrue(retained > LogcatBufferSize.toBytes(LogcatBufferSize.SMALL))
        assertTrue(retained <= LogcatBufferSize.toBytes(LogcatBufferSize.DEFAULT))
    }

    @Test
    fun `clearing empties the buffer and its measured size`() {
        val holder = LogcatSessionHolder()
        holder.fill(LEGACY_ENTRY_COUNT_CAP)
        holder.clearBuffer()
        assertTrue(holder.rawBuffer.isEmpty())
        holder.updateLimit(LogcatBufferSize.SMALL)
        holder.fill(1)
        assertEquals(1, holder.rawBuffer.size)
    }

    @Test
    fun `ids stay strictly increasing across a budget change`() {
        val holder = LogcatSessionHolder()
        val first = holder.nextId()
        holder.updateLimit(LogcatBufferSize.SMALL)
        val second = holder.nextId()
        assertTrue(second > first)
    }
}
