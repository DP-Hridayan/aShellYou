package `in`.hridayan.ashell.ai.presentation.model

import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity

data class AiChatUiState(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val currentSessionId: String? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val generatingSessionIds: Set<String> = emptySet(),
    val permissionPrompt: PermissionPrompt? = null,
    val runningTasks: List<RunningTaskUiModel> = emptyList()
)

data class PermissionPrompt(
    val command: String,
    val baseCommand: String,
    val onAllow: () -> Unit,
    val onAlwaysAllowExact: () -> Unit,
    val onAlwaysAllowBase: () -> Unit,
    val onDeny: () -> Unit
)

data class RunningTaskUiModel(
    val taskId: String,
    val name: String,
    val isRunning: Boolean,
    val sessionId: String? = null
)

data class Thought(
    val title: String,
    val detail: String
)

sealed class MessageComponent {
    data class Text(val text: String) : MessageComponent()
    data class CodeBlock(val language: String, val code: String) : MessageComponent()
}

sealed class ChatUiItem {
    abstract val id: String
    
    data class UserMessage(override val id: String, val content: String, val isOrphaned: Boolean = false) : ChatUiItem()
    data class ModelMessage(override val id: String, val content: String) : ChatUiItem()
    data class ThoughtGroup(override val id: String, val thoughts: List<Thought>, val isGenerating: Boolean = false) : ChatUiItem()
    data class LoadingDots(override val id: String = "loading_dots") : ChatUiItem()
}
