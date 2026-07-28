package `in`.hridayan.ashell.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "user" or "model"
    val rawContent: String, // Raw JSON string of the content (to preserve function calls and thought signatures)
    val timestamp: Long
)
