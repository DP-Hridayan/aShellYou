package `in`.hridayan.ashell.logcat.domain.repository

import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import kotlinx.coroutines.flow.Flow

/**
 * Saved filter profiles and the set of active ones. Both survive app restarts.
 */
interface LogcatFilterRepository {
    /** Saved profiles, in the order they were first saved. */
    fun profiles(): Flow<List<LogFilter>>

    /** Ids of the active profiles. */
    fun activeProfileIds(): Flow<Set<String>>

    /**
     * Adds [profile], or replaces the saved profile with the same id without moving it.
     * When [activate] is true the profile also becomes active, in the same write.
     */
    suspend fun saveProfile(profile: LogFilter, activate: Boolean): Result<Unit>

    /** Removes the profile and drops it from the active set. */
    suspend fun deleteProfile(id: String): Result<Unit>

    /**
     * Flips whether the profile is active. Done as one read-modify-write against storage, so two
     * quick taps on different profiles cannot overwrite each other.
     */
    suspend fun toggleActiveProfile(id: String): Result<Unit>
}
