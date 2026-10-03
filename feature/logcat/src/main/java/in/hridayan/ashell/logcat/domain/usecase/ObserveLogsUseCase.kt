package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.ResumePoint
import `in`.hridayan.ashell.logcat.domain.parser.LogcatParser
import `in`.hridayan.ashell.logcat.domain.util.dropResumeOverlap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

/**
 * Parses raw lines from a [LogcatEmitter] into [LogEntry] objects.
 *
 * IDs come from [LogcatSessionHolder.nextId], a counter shared by every stream that never
 * resets, so LazyColumn keys stay unique across restarts and across both tabs.
 */
class ObserveLogsUseCase @Inject constructor(
    private val sessionHolder: LogcatSessionHolder,
) {
    /**
     * @param resumeFrom where a previous stream of the same source stopped. When given, only
     *   newer entries are streamed. Null streams the source's whole buffer.
     */
    operator fun invoke(emitter: LogcatEmitter, resumeFrom: ResumePoint?): Flow<LogEntry> =
        emitter.lines(resumeFrom?.timestamp)
            .mapNotNull { line -> LogcatParser.parse(line, sessionHolder.nextId()) }
            .dropResumeOverlap(resumeFrom)
}
