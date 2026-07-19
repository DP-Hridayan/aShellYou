package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.matches
import `in`.hridayan.ashell.logcat.domain.parser.LogcatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class ObserveLogsUseCase @Inject constructor(
    private val emitter: LogcatEmitter,
) {
    private val idCounter = AtomicLong(0L)

    operator fun invoke(filter: LogFilter = LogFilter()): Flow<LogEntry> =
        emitter.lines()
            .mapNotNull { line -> LogcatParser.parse(line, idCounter.incrementAndGet()) }
            .filter { entry -> filter.matches(entry) }
}
