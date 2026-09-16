package `in`.hridayan.ashell.logcat.data.session

import `in`.hridayan.ashell.core.common.domain.model.LogcatBufferSize
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.util.approximateSizeBytes
import `in`.hridayan.ashell.logcat.service.LogcatService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

private const val INITIAL_BUFFER_CAPACITY = 512

/**
 * @Singleton bridge between [LogcatService] and [LogcatViewModel].
 *
 * Responsibilities:
 * 1. [rawBuffer] — persistent list of received [LogEntry]s, held within the memory
 *    budget set through [updateLimit]. Survives ViewModel recreation so logs persist
 *    across back-navigation.
 * 2. [entries] — live SharedFlow for new entries as they arrive.
 * 3. [isRunning] — authoritative service state visible to ALL ViewModels
 *    (HomeScreen's LogcatViewModel and LogcatScreen's LogcatViewModel share this).
 * 4. [nextId] — monotonically increasing ID, never resets even when the
 *    service is stopped and restarted. Prevents duplicate LazyColumn keys.
 * 5. [navigationEvents] — SharedFlow<Unit> for reactive deeplink navigation
 *    (notification tap / app shortcut). Replaces the boolean flag approach
 *    so it works even when the app is already running in the background.
 */
@Singleton
class LogcatSessionHolder @Inject constructor() {
    private val _entries = MutableSharedFlow<LogEntry>(extraBufferCapacity = 512)
    val entries: SharedFlow<LogEntry> = _entries.asSharedFlow()

    private val _rawBuffer = ArrayDeque<LogEntry>(INITIAL_BUFFER_CAPACITY)
    val rawBuffer: List<LogEntry> get() = synchronized(this) { _rawBuffer.toList() }

    private var limitBytes: Long = LogcatBufferSize.toBytes(LogcatBufferSize.DEFAULT)
    private var bufferBytes: Long = 0L

    fun appendToBuffer(entry: LogEntry) {
        synchronized(this) {
            _rawBuffer.addLast(entry)
            bufferBytes += entry.approximateSizeBytes()
            trimToLimit()
        }
    }

    /** Applies a new memory budget in megabytes, evicting the oldest entries if needed. */
    fun updateLimit(megabytes: Int) {
        synchronized(this) {
            limitBytes = LogcatBufferSize.toBytes(megabytes)
            trimToLimit()
        }
    }

    fun clearBuffer() = synchronized(this) {
        _rawBuffer.clear()
        bufferBytes = 0L
    }

    private fun trimToLimit() {
        while (_rawBuffer.size > 1 && bufferBytes > limitBytes) {
            bufferBytes -= _rawBuffer.removeFirst().approximateSizeBytes()
        }
    }

    private val idCounter = AtomicLong(0L)
    fun nextId(): Long = idCounter.incrementAndGet()

    // ── Authoritative running state (shared across all ViewModel instances) ─
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    private val _navigationChannel = Channel<Unit>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<Unit> = _navigationChannel.receiveAsFlow()

    fun triggerLogcatNavigation() {
        _navigationChannel.trySend(Unit)
    }

    suspend fun emit(entry: LogEntry) {
        appendToBuffer(entry)
        _entries.emit(entry)
    }

    fun tryEmit(entry: LogEntry): Boolean {
        appendToBuffer(entry)
        return _entries.tryEmit(entry)
    }
}
