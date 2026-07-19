package `in`.hridayan.ashell.logcat.domain.emitter

import kotlinx.coroutines.flow.Flow

/**
 * Generic contract for any log source.
 * Concrete implementations: [ShellLogcatEmitter], (future) ShizukuLogcatEmitter, etc.
 */
interface LogcatEmitter {
    /** Cold Flow of raw log lines. Each collection starts a fresh process. */
    fun lines(): Flow<String>
    fun isAvailable(): Boolean
}
