package `in`.hridayan.ashell.logcat.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.UUID

@Immutable
@Serializable
data class LogFilter(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val minLevel: LogLevel = LogLevel.VERBOSE,
    val pids: Set<String> = emptySet(),
    val tids: Set<String> = emptySet(),
    val uids: Set<String> = emptySet(),
    val packages: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val searchQuery: String = "",
    val mode: FilterMode = FilterMode.INCLUDE,
)

fun LogFilter.matches(entry: LogEntry): Boolean {
    val levelOk = entry.level.ordinal >= minLevel.ordinal
    val queryOk = searchQuery.isBlank() ||
            entry.message.contains(searchQuery, ignoreCase = true) ||
            entry.tag.contains(searchQuery, ignoreCase = true)
    val fieldMatch = (pids.isEmpty() || entry.pid in pids) &&
            (tids.isEmpty() || entry.tid in tids) &&
            (uids.isEmpty() || entry.uid in uids) &&
            (packages.isEmpty() || entry.packageName in packages) &&
            (tags.isEmpty() || entry.tag in tags)
    val baseMatch = levelOk && queryOk && fieldMatch
    return if (mode == FilterMode.INCLUDE) baseMatch else !baseMatch
}
