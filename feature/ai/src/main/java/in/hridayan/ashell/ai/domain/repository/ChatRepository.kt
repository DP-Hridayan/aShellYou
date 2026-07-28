package `in`.hridayan.ashell.ai.domain.repository

import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val streamingContents: StateFlow<Map<String, String?>>
    fun setStreamingContent(sessionId: String, content: String?)
    
    val activeGeneratingSessions: StateFlow<Set<String>>
    fun setGenerating(sessionId: String, isGenerating: Boolean)
    fun getAllSessions(): Flow<List<ChatSessionEntity>>
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>
    suspend fun getMessagesForSessionSync(sessionId: String): List<ChatMessageEntity>
    suspend fun getSessionByIdSync(sessionId: String): ChatSessionEntity?
    suspend fun createNewSession(sessionId: String, title: String): ChatSessionEntity
    suspend fun addMessage(message: ChatMessageEntity)
    suspend fun updateSessionTitle(sessionId: String, title: String)
    suspend fun updateSessionTitleByUser(sessionId: String, title: String)
    suspend fun updateSessionPinned(sessionId: String, isPinned: Boolean)
    suspend fun deleteSession(sessionId: String)
    suspend fun clearMessagesForSession(sessionId: String)
    suspend fun deleteMessage(messageId: String)
}
