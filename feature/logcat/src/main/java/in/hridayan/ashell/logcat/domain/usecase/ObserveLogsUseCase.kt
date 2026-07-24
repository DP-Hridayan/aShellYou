package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.parser.LogcatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * Parses raw lines from a [LogcatEmitter] into [LogEntry] objects.
 *
 * The emitter is now passed at call-site so the service can swap it
 * dynamically based on [LocalAdbWorkingMode] without restarting the use-case.
 *
 * IDs are sourced from [LogcatSessionHolder.nextId] — a singleton monotonically-
 * increasing counter — guaranteeing unique LazyColumn keys across service restarts.
 */
class ObserveLogsUseCase @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
) {
    operator fun invoke(emitter: LogcatEmitter): Flow<LogEntry> =
        emitter.lines()
            .mapNotNull { line -> LogcatParser.parse(line, sessionHolder.nextId()) }
}
