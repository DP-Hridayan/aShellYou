package `in`.hridayan.ashell.crashreporter.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import `in`.hridayan.ashell.crashreporter.data.model.CrashLogEntity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport

@Dao
interface CrashLogDao {
    @Insert
    suspend fun insertCrash(crash: CrashLogEntity)

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCrash(): CrashReport?

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
    suspend fun getAllCrashes(): List<CrashLogEntity>

    @Query("DELETE FROM crash_logs")
    suspend fun clearAllCrashes()
}
