package `in`.hridayan.ashell.logcat.domain.repository

import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import kotlinx.coroutines.flow.Flow

interface LogcatFilterRepository {
    fun getSavedFilters(): Flow<List<LogFilter>>
    suspend fun saveFilter(filter: LogFilter)
    suspend fun deleteFilter(id: String)
}
