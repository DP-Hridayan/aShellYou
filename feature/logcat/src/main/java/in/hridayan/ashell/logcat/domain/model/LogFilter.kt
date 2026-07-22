package `in`.hridayan.ashell.logcat.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Default set of levels shown in INCLUDE mode — all except Verbose.
 * User can expand to include Verbose or narrow down to just errors, etc.
 */
val DefaultIncludeLevels: Set<LogLevel> = setOf(
    LogLevel.DEBUG,
    LogLevel.INFO,
    LogLevel.WARNING,
    LogLevel.ERROR,
    LogLevel.FATAL,
)

@Immutable
@Serializable
data class LogFilter(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    /** Selected log levels. See [matches] for how this interacts with [mode]. */
    val levels: Set<LogLevel> = DefaultIncludeLevels,
    val pids: Set<String> = emptySet(),
    val tids: Set<String> = emptySet(),
    val uids: Set<String> = emptySet(),
    val packages: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val searchQuery: String = "",
    val mode: FilterMode = FilterMode.INCLUDE,
)

/**
 * Returns true if [entry] should be shown given this filter.
 *
 * **INCLUDE mode**
 * - `levels`: show entry only if its level is in the selected set.
 *   Empty set = show all levels (no restriction).
 * - `searchQuery`: show only entries containing the query in message or tag.
 *   Blank = no restriction.
 * - Field sets (pids/tids/uids/packages/tags): each non-empty set is a
 *   whitelist — entry must match. Empty set = no restriction for that dimension.
 *
 * **EXCLUDE mode**
 * - `levels`: hide entry if its level is in the selected set.
 *   Empty set = hide no levels (no restriction).
 * - `searchQuery`: hide entries containing the query. Blank = no restriction.
 * - Field sets: entry is hidden if it matches ANY non-empty specified dimension.
 */
fun LogFilter.matches(entry: LogEntry): Boolean {
    return when (mode) {
        FilterMode.INCLUDE -> {
            val levelOk = levels.isEmpty() || entry.level in levels
            val queryOk = searchQuery.isBlank() ||
                    entry.message.contains(searchQuery, ignoreCase = true) ||
                    entry.tag.contains(searchQuery, ignoreCase = true)
            val fieldOk = (pids.isEmpty() || entry.pid in pids) &&
                    (tids.isEmpty() || entry.tid in tids) &&
                    (uids.isEmpty() || entry.uid in uids) &&
                    (packages.isEmpty() || entry.packageName in packages) &&
                    (tags.isEmpty() || entry.tag in tags)
            levelOk && queryOk && fieldOk
        }

        FilterMode.EXCLUDE -> {
            // Level exclusion: hide if the level is in the selected set
            val levelExcluded = levels.isNotEmpty() && entry.level in levels
            // Query exclusion: hide if the query matches
            val queryExcluded = searchQuery.isNotBlank() && (
                    entry.message.contains(searchQuery, ignoreCase = true) ||
                            entry.tag.contains(searchQuery, ignoreCase = true))
            // Field exclusion: hide if any non-empty field set matches
            val fieldExcluded = (pids.isNotEmpty() && entry.pid in pids) ||
                    (tids.isNotEmpty() && entry.tid in tids) ||
                    (uids.isNotEmpty() && entry.uid in uids) ||
                    (packages.isNotEmpty() && entry.packageName in packages) ||
                    (tags.isNotEmpty() && entry.tag in tags)
            !(levelExcluded || queryExcluded || fieldExcluded)
        }
    }
}
