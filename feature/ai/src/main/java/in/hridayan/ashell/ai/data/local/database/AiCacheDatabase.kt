package `in`.hridayan.ashell.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for AI analysis cache.
 */
@Database(
    entities = [AiCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AiCacheDatabase : RoomDatabase() {
    abstract fun aiCacheDao(): AiCacheDao
}
