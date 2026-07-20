package `in`.hridayan.ashell.logcat.data.session

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
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
import `in`.hridayan.ashell.logcat.service.LogcatService

private const val MAX_RAW_BUFFER = 2000

/**
 * @Singleton bridge between [LogcatService] and [LogcatViewModel].
 *
 * Responsibilities:
 * 1. [rawBuffer] — persistent circular list of all received [LogEntry]s.
 *    Survives ViewModel recreation so logs persist across back-navigation.
 * 2. [entries] — live SharedFlow for new entries as they arrive.
 * 3. [isRunning] — authoritative service state visible to ALL ViewModels
 *    (HomeScreen's LogcatViewModel and LogcatScreen's LogcatViewModel share this).
 * 4. [nextId()] — monotonically increasing ID, never resets even when the
 *    service is stopped and restarted. Prevents duplicate LazyColumn keys.
 * 5. [navigationEvents] — SharedFlow<Unit> for reactive deeplink navigation
 *    (notification tap / app shortcut). Replaces the boolean flag approach
 *    so it works even when the app is already running in the background.
 */
@Singleton
class LogcatSessionHolder @Inject constructor() {
    private val _entries = MutableSharedFlow<LogEntry>(extraBufferCapacity = 512)
    val entries: SharedFlow<LogEntry> = _entries.asSharedFlow()

    private val _rawBuffer = ArrayDeque<LogEntry>(MAX_RAW_BUFFER)
    val rawBuffer: List<LogEntry> get() = synchronized(this) { _rawBuffer.toList() }

    fun appendToBuffer(entry: LogEntry) {
        synchronized(this) {
            if (_rawBuffer.size >= MAX_RAW_BUFFER) _rawBuffer.removeFirst()
            _rawBuffer.addLast(entry)
        }
    }

    fun clearBuffer() = synchronized(this) { _rawBuffer.clear() }

    private val idCounter = AtomicLong(0L)
    fun nextId(): Long = idCounter.incrementAndGet()

    // ── Authoritative running state (shared across all ViewModel instances) ─
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun setRunning(running: Boolean) { _isRunning.value = running }

    private val _navigationChannel = Channel<Unit>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<Unit> = _navigationChannel.receiveAsFlow()

    fun triggerLogcatNavigation() { _navigationChannel.trySend(Unit) }

    suspend fun emit(entry: LogEntry) {
        appendToBuffer(entry)
        _entries.emit(entry)
    }

    fun tryEmit(entry: LogEntry): Boolean {
        appendToBuffer(entry)
        return _entries.tryEmit(entry)
    }
}
