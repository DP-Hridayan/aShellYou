package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [LogcatFilterRepository] with the same semantics as the DataStore implementation. */
class FakeFilterRepository(
    profiles: List<LogFilter> = emptyList(),
    activeIds: Set<String> = emptySet(),
) : LogcatFilterRepository {
    val profiles = MutableStateFlow(profiles)
    val activeIds = MutableStateFlow(activeIds)

    /** When set, every write fails with this error and changes nothing. */
    var failure: Throwable? = null

    override fun profiles(): Flow<List<LogFilter>> = profiles

    override fun activeProfileIds(): Flow<Set<String>> = activeIds

    override suspend fun saveProfile(profile: LogFilter, activate: Boolean): Result<Unit> = write {
        profiles.update { current ->
            val index = current.indexOfFirst { it.id == profile.id }
            if (index < 0) current + profile else current.toMutableList().apply { set(index, profile) }
        }
        if (activate) activeIds.update { it + profile.id }
    }

    override suspend fun deleteProfile(id: String): Result<Unit> = write {
        profiles.update { current -> current.filterNot { it.id == id } }
        activeIds.update { it - id }
    }

    override suspend fun toggleActiveProfile(id: String): Result<Unit> = write {
        activeIds.update { if (id in it) it - id else it + id }
    }

    private fun write(change: () -> Unit): Result<Unit> {
        failure?.let { return Result.failure(it) }
        change()
        return Result.success(Unit)
    }
}
