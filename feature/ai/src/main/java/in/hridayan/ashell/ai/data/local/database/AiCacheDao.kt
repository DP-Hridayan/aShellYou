package `in`.hridayan.ashell.ai.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for the AI analysis cache.
 */
@Dao
interface AiCacheDao {

    @Query("SELECT * FROM ai_analysis_cache WHERE commandHash = :hash AND modelId = :modelId LIMIT 1")
    suspend fun getByCommandHashAndModel(hash: String, modelId: String): AiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiCacheEntity)

    @Query("DELETE FROM ai_analysis_cache WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM ai_analysis_cache")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM ai_analysis_cache")
    suspend fun getCacheCount(): Int

    @Query("SELECT COALESCE(SUM(LENGTH(analysisJson)), 0) FROM ai_analysis_cache")
    suspend fun getCacheSizeBytes(): Long
}
