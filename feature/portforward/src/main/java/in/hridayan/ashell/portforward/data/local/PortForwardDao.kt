package `in`.hridayan.ashell.portforward.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTunnelDao {

    @Query("SELECT * FROM saved_tunnels ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<SavedTunnelEntity>>

    @Query("SELECT * FROM saved_tunnels ORDER BY createdAt ASC")
    suspend fun getAll(): List<SavedTunnelEntity>

    @Query("SELECT * FROM saved_tunnels WHERE id = :id")
    suspend fun getById(id: String): SavedTunnelEntity?

    @Query("SELECT id FROM saved_tunnels WHERE wasRunning = 1")
    fun observeWasRunningIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(tunnel: SavedTunnelEntity)

    @Upsert
    suspend fun upsertAll(tunnels: List<SavedTunnelEntity>)

    @Query("DELETE FROM saved_tunnels WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM saved_tunnels")
    suspend fun deleteAll()

    @Query("UPDATE saved_tunnels SET wasRunning = :wasRunning WHERE id IN (:ids)")
    suspend fun setWasRunning(ids: List<String>, wasRunning: Boolean)
}

@Dao
interface RecentHostDao {

    @Query("SELECT * FROM recent_hosts ORDER BY lastUsedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentHostEntity>>

    @Query("SELECT * FROM recent_hosts ORDER BY lastUsedAt DESC")
    suspend fun getAll(): List<RecentHostEntity>

    @Upsert
    suspend fun upsert(host: RecentHostEntity)

    @Upsert
    suspend fun upsertAll(hosts: List<RecentHostEntity>)

    @Query(
        "DELETE FROM recent_hosts WHERE host NOT IN " +
            "(SELECT host FROM recent_hosts ORDER BY lastUsedAt DESC LIMIT :keep)"
    )
    suspend fun trim(keep: Int)

    @Query("DELETE FROM recent_hosts")
    suspend fun deleteAll()
}
