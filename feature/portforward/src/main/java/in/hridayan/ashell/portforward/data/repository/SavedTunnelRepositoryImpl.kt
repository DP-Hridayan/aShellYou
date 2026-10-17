package `in`.hridayan.ashell.portforward.data.repository

import android.util.Log
import `in`.hridayan.ashell.portforward.data.local.PortForwardDatabase
import `in`.hridayan.ashell.portforward.data.local.RecentHostEntity
import `in`.hridayan.ashell.portforward.data.local.toDomain
import `in`.hridayan.ashell.portforward.data.local.toEntity
import `in`.hridayan.ashell.portforward.data.local.toSpec
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavedTunnelRepositoryImpl @Inject constructor(
    database: PortForwardDatabase
) : SavedTunnelRepository {

    private val tunnelDao = database.savedTunnelDao()
    private val hostDao = database.recentHostDao()

    override fun tunnels(): Flow<List<TunnelSpec>> =
        tunnelDao.observeAll().map { rows -> rows.mapNotNull { it.toSpec() } }

    override fun wasRunningIds(): Flow<Set<String>> = tunnelDao.observeWasRunningIds().map { it.toSet() }

    override suspend fun tunnel(id: String): Result<TunnelSpec?> = attempt {
        tunnelDao.getById(id)?.toSpec()
    }

    override suspend fun save(spec: TunnelSpec): Result<Unit> = attempt {
        tunnelDao.upsert(spec.toEntity(tunnelDao.getById(spec.id), System.currentTimeMillis()))
    }

    override suspend fun delete(id: String): Result<Unit> = attempt { tunnelDao.delete(id) }

    override suspend fun setWasRunning(ids: Collection<String>, wasRunning: Boolean): Result<Unit> = attempt {
        if (ids.isNotEmpty()) tunnelDao.setWasRunning(ids.toList(), wasRunning)
    }

    override fun recentHosts(): Flow<List<RecentHost>> =
        hostDao.observeRecent(RECENT_HOST_LIMIT).map { rows -> rows.map { it.toDomain() } }

    override suspend fun rememberHost(host: RecentHost): Result<Unit> = attempt {
        hostDao.upsert(RecentHostEntity(host.host, host.lastPort, System.currentTimeMillis()))
        hostDao.trim(RECENT_HOST_LIMIT)
    }

    override suspend fun clearRecentHosts(): Result<Unit> = attempt { hostDao.deleteAll() }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Port forward database operation failed", e)
        Result.failure(e)
    }

    private companion object {
        const val TAG = "SavedTunnelRepository"
        const val RECENT_HOST_LIMIT = 10
    }
}
