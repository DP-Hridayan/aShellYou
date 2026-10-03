package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val WINDOW_MS = 50L
private const val SETTLE_MS = 1_000L
private const val BUDGET_BYTES = 1_000_000L
private const val KEPT_TAG = "keep"
private const val OTHER_TAG = "other"

private fun entry(id: Long, tag: String = KEPT_TAG) = LogEntry(
    id = id,
    timestamp = "10-03 10:00:%02d.000".format(id),
    pid = "1",
    tid = "1",
    uid = "",
    packageName = "",
    level = LogLevel.INFO,
    tag = tag,
    message = "message $id",
)

private object TestEmitter : LogcatEmitter {
    override fun lines(since: String?): Flow<String> = emptyFlow()
    override fun isAvailable(): Boolean = true
}

/** Stands in for the use case: records each start and serves the next prepared stream. */
private class FakeSource {
    val starts = mutableListOf<ResumePoint?>()
    val emitters = mutableListOf<LogcatEmitter>()
    var nextStream: Flow<LogEntry> = emptyFlow()

    fun observe(emitter: LogcatEmitter, resumeFrom: ResumePoint?): Flow<LogEntry> {
        emitters += emitter
        starts += resumeFrom
        return nextStream
    }
}

private fun openStream(vararg entries: LogEntry): Flow<LogEntry> = flow {
    entries.forEach { emit(it) }
    awaitCancellation()
}

/**
 * The session runs in `backgroundScope`, whose work `advanceUntilIdle` does not wait for, so the
 * virtual clock is moved past every batching window explicitly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.settle() {
    advanceTimeBy(SETTLE_MS)
    runCurrent()
}

private fun ids(session: OtherDeviceSession): List<Long> = session.state.value.logs.map { it.id }

@OptIn(ExperimentalCoroutinesApi::class)
class OtherDeviceSessionTest {

    private val source = FakeSource()
    private var filter = LogFilter()

    private fun TestScope.newSession(scope: CoroutineScope = backgroundScope) = OtherDeviceSession(
        scope = scope,
        observeLogs = source::observe,
        currentFilter = { filter },
        batchWindowMs = WINDOW_MS,
        maxBytes = BUDGET_BYTES,
    )

    @Test
    fun `connecting streams the whole buffer and reports running`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1), entry(2))
        session.connect(TestEmitter)
        settle()
        assertEquals(listOf(1L, 2L), ids(session))
        assertTrue(session.isRunning.value)
        assertEquals(listOf<ResumePoint?>(null), source.starts)
        assertEquals(listOf<LogcatEmitter>(TestEmitter), source.emitters)
    }

    @Test
    fun `stopping keeps the collected logs`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1))
        session.connect(TestEmitter)
        settle()
        session.stop()
        settle()
        assertEquals(listOf(1L), ids(session))
        assertFalse(session.isRunning.value)
    }

    @Test
    fun `playing after a stop resumes where the stream stopped`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1), entry(2))
        session.connect(TestEmitter)
        settle()
        session.stop()
        source.nextStream = openStream(entry(3))
        session.play()
        settle()
        assertEquals(entry(2).timestamp, source.starts.last()?.timestamp)
        assertEquals(listOf(1L, 2L, 3L), ids(session))
        assertTrue(session.isRunning.value)
    }

    @Test
    fun `playing resumes following`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1))
        session.connect(TestEmitter)
        settle()
        session.pauseAutoScroll()
        session.stop()
        session.play()
        assertTrue(session.state.value.isAutoScrolling)
    }

    @Test
    fun `reconnecting starts a fresh session`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1))
        session.connect(TestEmitter)
        settle()
        source.nextStream = openStream(entry(5))
        session.connect(TestEmitter)
        settle()
        assertEquals(listOf(5L), ids(session))
        assertNull(source.starts.last())
    }

    @Test
    fun `a stream that ends by itself is reported as stopped and keeps its logs`() = runTest {
        val session = newSession()
        source.nextStream = flow { emit(entry(1)) }
        session.connect(TestEmitter)
        settle()
        assertFalse(session.isRunning.value)
        assertEquals(listOf(1L), ids(session))
    }

    @Test
    fun `the active filter applies to incoming entries`() = runTest {
        val session = newSession()
        filter = LogFilter(tags = setOf(KEPT_TAG))
        source.nextStream = openStream(entry(1), entry(2, OTHER_TAG), entry(3))
        session.connect(TestEmitter)
        settle()
        assertEquals(listOf(1L, 3L), ids(session))
    }

    @Test
    fun `reapplying the filter re-filters everything collected`() = runTest {
        val session = newSession()
        filter = LogFilter(tags = setOf(KEPT_TAG))
        source.nextStream = openStream(entry(1), entry(2, OTHER_TAG))
        session.connect(TestEmitter)
        settle()
        filter = LogFilter(tags = setOf(OTHER_TAG))
        session.reapplyFilter()
        assertEquals(listOf(2L), ids(session))
    }

    @Test
    fun `clearing empties the list and play does not read it back`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1))
        session.connect(TestEmitter)
        settle()
        session.stop()
        session.clear()
        source.nextStream = openStream()
        session.play()
        settle()
        assertTrue(ids(session).isEmpty())
        assertNotNull(source.starts.last())
    }

    @Test
    fun `play without a connected device does nothing`() = runTest {
        val session = newSession()
        session.play()
        settle()
        assertTrue(source.starts.isEmpty())
        assertFalse(session.isRunning.value)
    }

    @Test
    fun `disconnecting stops the stream and keeps its logs`() = runTest {
        val session = newSession()
        source.nextStream = openStream(entry(1))
        session.connect(TestEmitter)
        settle()
        session.disconnect()
        session.play()
        settle()
        assertEquals(listOf(1L), ids(session))
        assertFalse(session.isRunning.value)
        assertEquals(1, source.starts.size)
    }
}
