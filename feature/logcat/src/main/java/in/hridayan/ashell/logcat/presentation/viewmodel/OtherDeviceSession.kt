package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.FilterCriteria
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint
import `in`.hridayan.ashell.logcat.domain.util.CappedLog
import `in`.hridayan.ashell.logcat.domain.util.ResumePointTracker
import `in`.hridayan.ashell.logcat.domain.util.append
import `in`.hridayan.ashell.logcat.domain.util.approximateSizeBytes
import `in`.hridayan.ashell.logcat.domain.util.batchByTime
import `in`.hridayan.ashell.logcat.domain.util.trimmedTo
import `in`.hridayan.ashell.logcat.presentation.model.LogListUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * The log stream of an external device connected over OTG or Wi-Fi ADB, as shown in the Other
 * Device tab.
 *
 * Unlike this device's logs, which live in a foreground service, this stream lives only as long
 * as [scope]. It keeps an unfiltered copy of everything it collected so a filter change can be
 * re-applied, and remembers where it stopped so [play] does not read the device's buffer again.
 *
 * [connect] starts a new session for a newly connected device and reads its whole buffer.
 * [stop] and [disconnect] keep what was collected; only [clear] and the next [connect] discard it.
 * Call every method from the thread [scope] dispatches on.
 */
class OtherDeviceSession(
    private val scope: CoroutineScope,
    private val observeLogs: (LogcatEmitter, ResumePoint?) -> Flow<LogEntry>,
    private val currentCriteria: () -> FilterCriteria,
    private val batchWindowMs: Long,
    maxBytes: Long,
) {
    private val list = LogListStateStore(maxBytes)
    val state: StateFlow<LogListUiState> = list.state

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val sizeOf: (LogEntry) -> Int = LogEntry::approximateSizeBytes
    private val resumeTracker = ResumePointTracker()
    private var limitBytes: Long = maxBytes
    private var collected: CappedLog<LogEntry> = CappedLog()
    private var source: LogcatEmitter? = null
    private var streamJob: Job? = null

    fun connect(emitter: LogcatEmitter) {
        stop()
        source = emitter
        collected = CappedLog()
        resumeTracker.reset()
        list.reset()
        launchStream(emitter, resumeFrom = null)
    }

    fun disconnect() {
        stop()
        source = null
    }

    fun play() {
        val emitter = source ?: return
        if (streamJob != null) return
        list.resume()
        launchStream(emitter, resumeTracker.current())
    }

    fun stop() {
        streamJob?.cancel()
        streamJob = null
        _isRunning.value = false
    }

    fun clear() {
        collected = CappedLog()
        list.reset()
    }

    fun reapplyFilter() {
        list.replace(visibleOf(collected.items))
    }

    fun updateLimit(maxBytes: Long) {
        limitBytes = maxBytes
        collected = collected.trimmedTo(maxBytes, sizeOf)
        list.updateLimit(maxBytes)
    }

    fun pauseAutoScroll() = list.pause()

    fun resumeAutoScroll() = list.resume()

    private fun launchStream(emitter: LogcatEmitter, resumeFrom: ResumePoint?) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                collectStream(emitter, resumeFrom)
            } finally {
                markEndedIfCurrent(coroutineContext.job)
            }
        }
        streamJob = job
        _isRunning.value = true
        job.start()
    }

    private suspend fun collectStream(emitter: LogcatEmitter, resumeFrom: ResumePoint?) {
        observeLogs(emitter, resumeFrom)
            .onEach { resumeTracker.record(it) }
            .batchByTime(batchWindowMs)
            .collect { onBatch(it) }
    }

    /**
     * A stream that was stopped and replaced must not mark its successor as stopped, so only the
     * current job may clear the running flag when it ends.
     */
    private fun markEndedIfCurrent(job: Job) {
        if (streamJob !== job) return
        streamJob = null
        _isRunning.value = false
    }

    private fun onBatch(batch: List<LogEntry>) {
        collected = collected.append(batch, limitBytes, sizeOf)
        val visible = visibleOf(batch)
        if (visible.isNotEmpty()) list.append(visible)
    }

    private fun visibleOf(entries: List<LogEntry>): List<LogEntry> {
        val criteria = currentCriteria()
        return entries.filter { criteria.matches(it) }
    }
}
