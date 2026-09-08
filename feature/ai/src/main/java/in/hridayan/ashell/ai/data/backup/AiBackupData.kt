package `in`.hridayan.ashell.ai.data.backup

import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.CommandPermissionEntity
import kotlinx.serialization.Serializable

@Serializable
data class AiBackupData(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val commandPermissions: List<CommandPermissionEntity> = emptyList()
)
