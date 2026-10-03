package `in`.hridayan.ashell.logcat.domain.model

/**
 * What one log list currently shows.
 *
 * A log is shown when any of [activeProfiles] shows it, so when two profiles disagree the log is
 * included. With no active profile every log is shown. [searchQuery] then narrows the result by
 * message or tag, ignoring case.
 *
 * Profiles are shared by both tabs, but a package resolves to a different UID on each device, so
 * each tab builds its own criteria with the [packageUids] of its own device.
 */
data class FilterCriteria(
    val activeProfiles: List<LogFilter> = emptyList(),
    val searchQuery: String = "",

    /** UID of each installed package named by [activeProfiles]; missing packages are left out. */
    val packageUids: Map<String, String> = emptyMap(),
) {
    /** Every package named by an active profile. */
    val packages: Set<String> get() = activeProfiles.flatMapTo(mutableSetOf()) { it.packages }

    private val profileUids: List<Pair<LogFilter, Set<String>>> by lazy {
        activeProfiles.map { profile -> profile to profile.packages.mapNotNullTo(mutableSetOf()) { packageUids[it] } }
    }

    fun matches(entry: LogEntry): Boolean = isShownByProfiles(entry) && matchesSearch(entry)

    private fun isShownByProfiles(entry: LogEntry): Boolean =
        profileUids.isEmpty() || profileUids.any { (profile, uids) -> profile.matches(entry, uids) }

    private fun matchesSearch(entry: LogEntry): Boolean =
        searchQuery.isBlank() ||
            entry.message.contains(searchQuery, ignoreCase = true) ||
            entry.tag.contains(searchQuery, ignoreCase = true)
}
