package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.GithubRepoStats
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import kotlinx.coroutines.flow.Flow

interface GithubDataRepository {
    suspend fun fetchLatestRelease(includePrerelease: Boolean, releaseType: Int): UpdateResult
    fun observeRepoStats(): Flow<GithubRepoStats>
    suspend fun refreshRepoStats()
}