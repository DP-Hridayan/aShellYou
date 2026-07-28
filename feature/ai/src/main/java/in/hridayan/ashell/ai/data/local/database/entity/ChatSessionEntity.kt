package `in`.hridayan.ashell.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val isUserRenamed: Boolean = false
)
