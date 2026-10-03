package `in`.hridayan.ashell.logcat.data.repository

import `in`.hridayan.ashell.logcat.domain.model.LogFilter

internal fun List<LogFilter>.upsert(profile: LogFilter): List<LogFilter> {
    val index = indexOfFirst { it.id == profile.id }
    if (index < 0) return this + profile
    return toMutableList().apply { set(index, profile) }
}

internal fun Set<String>.toggled(id: String): Set<String> = if (id in this) this - id else this + id
