package `in`.hridayan.ashell.crashreporter.domain.repository

import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import kotlinx.coroutines.flow.Flow

interface CrashRepository {
    suspend fun addCrash(crash: CrashReport)
    fun getCrashById(id: Long): Flow<CrashReport?>
    suspend fun getLatestCrash(): CrashReport?
    fun getAllCrashes(): Flow<List<CrashReport>>
    suspend fun clearAllCrashes()
}
