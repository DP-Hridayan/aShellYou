package `in`.hridayan.ashell.logcat.domain.emitter

import kotlinx.coroutines.flow.Flow

/**
 * A source of raw logcat lines, such as a local process, a Shizuku process, or a remote device.
 */
interface LogcatEmitter {
    /**
     * Cold flow of raw threadtime lines. Each collection starts a fresh logcat process.
     *
     * @param since a threadtime timestamp, `MM-DD HH:MM:SS.mmm`, to start from instead of the
     *   device's whole buffer. Entries at exactly that time are included. Null reads everything.
     */
    fun lines(since: String?): Flow<String>
    fun isAvailable(): Boolean
}
