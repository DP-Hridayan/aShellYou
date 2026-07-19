package `in`.hridayan.ashell.logcat.data.session

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton bridge between [LogcatService] (producer) and [LogcatViewModel] (consumer).
 *
 * The service emits [LogEntry] items here; the ViewModel subscribes to [entries].
 * Using a SharedFlow with a replay buffer ensures entries emitted just before
 * the ViewModel subscribes are not lost.
 */
@Singleton
class LogcatSessionHolder @Inject constructor() {

    private val _entries = MutableSharedFlow<LogEntry>(extraBufferCapacity = 512)
    val entries: SharedFlow<LogEntry> = _entries.asSharedFlow()

    suspend fun emit(entry: LogEntry) {
        _entries.emit(entry)
    }

    fun tryEmit(entry: LogEntry): Boolean = _entries.tryEmit(entry)
}
