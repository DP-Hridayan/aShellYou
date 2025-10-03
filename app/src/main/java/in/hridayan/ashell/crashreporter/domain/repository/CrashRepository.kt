package `in`.hridayan.ashell.crashreporter.domain.repository

import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport

interface CrashRepository {
    suspend fun addCrash(crash: CrashReport)
    suspend fun getLatestCrash(): CrashReport?
    suspend fun getAllCrashes(): List<CrashReport>
    suspend fun clearAllCrashes()
}
