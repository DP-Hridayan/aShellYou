package `in`.hridayan.ashell.qstiles.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TileLogDao {

    @Insert
    suspend fun insert(log: TileLogEntity)

    @Query("SELECT * FROM tile_logs WHERE tileId = :tileId ORDER BY timestamp DESC")
    fun getLogsForTile(tileId: Int): Flow<List<TileLogEntity>>

    @Query("SELECT * FROM tile_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TileLogEntity>>

    @Query("SELECT COUNT(*) FROM tile_logs")
    fun getTotalExecutions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tile_logs WHERE isSuccess = 1")
    fun getSuccessCount(): Flow<Int>
}