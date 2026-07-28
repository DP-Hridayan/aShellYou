package `in`.hridayan.ashell.ai.data.repository

import `in`.hridayan.ashell.ai.data.local.database.dao.ChatDao
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    private val _streamingContents = MutableStateFlow<Map<String, String?>>(emptyMap())
    override val streamingContents: StateFlow<Map<String, String?>> = _streamingContents.asStateFlow()

    override fun setStreamingContent(sessionId: String, content: String?) {
        _streamingContents.update { current ->
            current.toMutableMap().apply {
                if (content == null) remove(sessionId) else put(sessionId, content)
            }
        }
    }
    
    private val _activeGeneratingSessions = MutableStateFlow<Set<String>>(emptySet())
    override val activeGeneratingSessions: StateFlow<Set<String>> = _activeGeneratingSessions.asStateFlow()
    
    override fun setGenerating(sessionId: String, isGenerating: Boolean) {
        _activeGeneratingSessions.update { current ->
            if (isGenerating) current + sessionId else current - sessionId
        }
    }

    override fun getAllSessions(): Flow<List<ChatSessionEntity>> {
        return chatDao.getAllSessions()
    }

    override fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    override suspend fun getMessagesForSessionSync(sessionId: String): List<ChatMessageEntity> {
        return chatDao.getMessagesForSessionSync(sessionId)
    }

    override suspend fun getSessionByIdSync(sessionId: String): ChatSessionEntity? {
        return chatDao.getSessionById(sessionId)
    }

    override suspend fun createNewSession(sessionId: String, title: String): ChatSessionEntity {
        val session = ChatSessionEntity(
            id = sessionId,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        return session
    }

    override suspend fun addMessage(message: ChatMessageEntity) {
        chatDao.insertMessageAndUpdateSession(message)
    }

    override suspend fun updateSessionTitle(sessionId: String, title: String) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    override suspend fun updateSessionTitleByUser(sessionId: String, title: String) {
        chatDao.updateSessionTitleByUser(sessionId, title)
    }

    override suspend fun updateSessionPinned(sessionId: String, isPinned: Boolean) {
        chatDao.updateSessionPinned(sessionId, isPinned)
    }

    override suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    override suspend fun clearMessagesForSession(sessionId: String) {
        chatDao.clearMessagesForSession(sessionId)
    }

    override suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessage(messageId)
    }
}
