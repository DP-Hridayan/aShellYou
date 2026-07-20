package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.parser.LogcatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * Parses raw lines from [LogcatEmitter] into [LogEntry] objects.
 *
 * IDs are sourced from [LogcatSessionHolder.nextId] which is a @Singleton
 * monotonically-increasing counter. This guarantees IDs never reset even
 * when the service is stopped and restarted — preventing duplicate
 * LazyColumn keys that cause runtime crashes.
 */
class ObserveLogsUseCase @Inject constructor(
    private val emitter: LogcatEmitter,
    private val sessionHolder: LogcatSessionHolder,
) {
    operator fun invoke(): Flow<LogEntry> =
        emitter.lines()
            .mapNotNull { line -> LogcatParser.parse(line, sessionHolder.nextId()) }
}
