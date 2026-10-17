package `in`.hridayan.ashell.logcat.domain.repository

import `in`.hridayan.ashell.logcat.domain.model.ExportTarget
import `in`.hridayan.ashell.logcat.domain.model.LogEntry

interface LogExportRepository {

    /**
     * Writes [entries] as logcat text to [target] and returns the content URI of the written file,
     * or a failure when the target cannot be opened or written.
     */
    suspend fun writeTo(target: ExportTarget, entries: List<LogEntry>): Result<String>
}
