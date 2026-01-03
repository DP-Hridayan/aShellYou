package `in`.hridayan.ashell.core.data.remote.repository

import android.util.Log
import `in`.hridayan.ashell.core.data.local.database.GithubRepoStatsDao
import `in`.hridayan.ashell.core.data.local.mapper.toDomain
import `in`.hridayan.ashell.core.data.local.mapper.toEntity
import `in`.hridayan.ashell.core.data.remote.api.GithubApi
import `in`.hridayan.ashell.core.data.remote.mapper.mapToRepoStats
import `in`.hridayan.ashell.core.data.remote.mapper.toDomain
import `in`.hridayan.ashell.core.domain.model.GithubRepoStats
import `in`.hridayan.ashell.core.domain.repository.GithubDataRepository
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import javax.inject.Inject

class GithubDataRepositoryImpl @Inject constructor(
    private val api: GithubApi,
    private val dao: GithubRepoStatsDao,
) : GithubDataRepository {
    private val repoKey = "DP-Hridayan/aShellYou"

    override suspend fun fetchLatestRelease(includePrerelease: Boolean): UpdateResult {
        return try {
            Log.d("GithubDataRepository", "Fetching latest release from Github")
            val response = api.fetchLatestRelease(includePrerelease).toDomain()
            UpdateResult.Success(response, response.tagName.isNotEmpty())
        } catch (e: SocketTimeoutException) {
            Log.e("GithubDataRepository", "Timeout", e)
            UpdateResult.Timeout
        } catch (e: UnknownHostException) {
            Log.e("GithubDataRepository", "No internet (UnknownHost)", e)
            UpdateResult.NetworkError
        } catch (e: ConnectException) {
            Log.e("GithubDataRepository", "No internet (ConnectException)", e)
            UpdateResult.NetworkError
        } catch (e: UnresolvedAddressException) {
            Log.e("GithubDataRepository", "No internet (UnresolvedAddressException)", e)
            UpdateResult.NetworkError
        } catch (e: IOException) {
            Log.e("GithubDataRepository", "IO Exception", e)
            UpdateResult.NetworkError
        } catch (e: Exception) {
            Log.e("GithubDataRepository", "Unknown error", e)
            UpdateResult.UnknownError
        }
    }

    override fun observeRepoStats(): Flow<GithubRepoStats> {
        return dao.observe(repoKey)
            .filterNotNull()
            .map { it.toDomain() }
    }

    override suspend fun refreshRepoStats() {
        withContext(Dispatchers.IO) {
            try {
                val repoInfo = api.fetchRepoStats()
                val allReleases = api.fetchAllReleases()

                val stats = mapToRepoStats(repoInfo, allReleases)

                dao.insert(stats.toEntity(repoKey))
            } catch (e: Exception) {
                Log.d("refreshRepoStats [GithubDataRepositoryImpl]", e.toString())
            }
        }
    }
}