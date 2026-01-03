package `in`.hridayan.ashell.core.domain.repository

import `in`.hridayan.ashell.core.domain.model.GithubRepoStats
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import kotlinx.coroutines.flow.Flow

interface GithubDataRepository {
    suspend fun fetchLatestRelease(includePrerelease: Boolean): UpdateResult
    fun observeRepoStats(): Flow<GithubRepoStats>
    suspend fun refreshRepoStats()
}