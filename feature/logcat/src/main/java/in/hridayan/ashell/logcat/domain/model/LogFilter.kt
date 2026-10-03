package `in`.hridayan.ashell.logcat.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Levels a new Include profile starts with: everything except Verbose.
 */
val DefaultIncludeLevels: Set<LogLevel> = setOf(
    LogLevel.DEBUG,
    LogLevel.INFO,
    LogLevel.WARNING,
    LogLevel.ERROR,
    LogLevel.FATAL,
)

/**
 * A saved filter profile. Several can be active at once; see [FilterCriteria] for how they
 * combine.
 *
 * Profiles are persisted as JSON. Fields removed from this class, such as UID and search query in
 * older saved profiles, are ignored when those profiles are read back.
 */
@Immutable
@Serializable
data class LogFilter(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",

    /** Selected log levels. See [matches] for how this interacts with [mode]. */
    val levels: Set<LogLevel> = DefaultIncludeLevels,
    val pids: Set<String> = emptySet(),
    val tids: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),

    /**
     * Package names. Log lines carry a UID rather than a package, so these are matched through the
     * UIDs they resolve to on the device being filtered; see [FilterCriteria].
     */
    val packages: Set<String> = emptySet(),
    val mode: FilterMode = FilterMode.INCLUDE,
)

/**
 * Returns true if [entry] should be shown under this profile alone. [packageUids] are the UIDs
 * this profile's [LogFilter.packages] resolve to on the device the entry came from.
 *
 * **INCLUDE mode**: the entry must have a selected level, and must match every non-empty list of
 * PIDs, TIDs, tags and packages. An empty list, including an empty level set, does not restrict.
 * A package that resolved to no UID matches nothing.
 *
 * **EXCLUDE mode**: the entry is hidden when its level is selected, or when it matches any
 * non-empty list of PIDs, TIDs, tags or packages.
 */
fun LogFilter.matches(entry: LogEntry, packageUids: Set<String>): Boolean = when (mode) {
    FilterMode.INCLUDE -> includes(entry, packageUids)
    FilterMode.EXCLUDE -> !excludes(entry, packageUids)
}

private fun LogFilter.includes(entry: LogEntry, packageUids: Set<String>): Boolean =
    levels.admits(entry.level) &&
        pids.admits(entry.pid) &&
        tids.admits(entry.tid) &&
        tags.admits(entry.tag) &&
        (packages.isEmpty() || entry.uid in packageUids)

/** An empty set places no restriction on that dimension. */
private fun <T> Set<T>.admits(value: T): Boolean = isEmpty() || value in this

private fun LogFilter.excludes(entry: LogEntry, packageUids: Set<String>): Boolean =
    entry.level in levels ||
        entry.pid in pids ||
        entry.tid in tids ||
        entry.tag in tags ||
        entry.uid in packageUids
