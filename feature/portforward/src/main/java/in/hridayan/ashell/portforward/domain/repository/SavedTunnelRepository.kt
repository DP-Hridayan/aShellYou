package `in`.hridayan.ashell.portforward.domain.repository

import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import kotlinx.coroutines.flow.Flow

/** The tunnels the user has saved, and the hosts they relayed to recently. */
interface SavedTunnelRepository {

    /** Saved tunnels, oldest first. */
    fun tunnels(): Flow<List<TunnelSpec>>

    /**
     * Ids of tunnels that were running when the app last stopped them. A tunnel in this set that is
     * not running now was stopped by the system, not by the user.
     */
    fun wasRunningIds(): Flow<Set<String>>

    suspend fun tunnel(id: String): Result<TunnelSpec?>

    suspend fun save(spec: TunnelSpec): Result<Unit>

    suspend fun delete(id: String): Result<Unit>

    suspend fun setWasRunning(ids: Collection<String>, wasRunning: Boolean): Result<Unit>

    fun recentHosts(): Flow<List<RecentHost>>

    suspend fun rememberHost(host: RecentHost): Result<Unit>

    suspend fun clearRecentHosts(): Result<Unit>
}
