package `in`.hridayan.ashell.crashreporter.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings
import `in`.hridayan.ashell.crashreporter.data.model.CrashLogEntity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashLogDao {
    @Insert
    suspend fun insertCrash(crash: CrashLogEntity)

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCrash(): CrashReport?

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
    fun getAllCrashes(): Flow<List<CrashLogEntity>>

    @Query("DELETE FROM crash_logs")
    suspend fun clearAllCrashes()
}
