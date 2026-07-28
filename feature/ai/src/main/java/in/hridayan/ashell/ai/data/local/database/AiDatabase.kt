package `in`.hridayan.ashell.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.ai.data.local.database.dao.ChatDao
import `in`.hridayan.ashell.ai.data.local.database.dao.CommandPermissionDao
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.CommandPermissionEntity

/**
 * Room database for AI feature, including cache, chat sessions, and permissions.
 */
@Database(
    entities = [
        AiCacheEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        CommandPermissionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AiDatabase : RoomDatabase() {
    abstract fun aiCacheDao(): AiCacheDao
    abstract fun chatDao(): ChatDao
    abstract fun commandPermissionDao(): CommandPermissionDao
}
