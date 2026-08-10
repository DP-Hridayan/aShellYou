package `in`.hridayan.ashell.ai.domain.manager

import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.usecase.GenerateAiChatResponseUseCase
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSessionManager @Inject constructor(
    private val chatRepository: ChatRepository,
    private val generateAiChatResponseUseCase: GenerateAiChatResponseUseCase
) {
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val generationJobs = ConcurrentHashMap<String, Job>()
    private val json = Json {
        ignoreUnknownKeys = true;
        encodeDefaults = true
    }

    var activeSessionId: String? = null

    fun sendMessage(sessionId: String, onStart: suspend () -> Unit = {}) {
        generationJobs[sessionId]?.cancel()

        val job = managerScope.launch {
            chatRepository.setGenerating(sessionId, true)
            try {
                onStart()
                generateAiChatResponseUseCase(sessionId)
            } catch (e: Exception) {
                // Log and show error in chat
                val errorMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "model",
                    rawContent = json.encodeToString(
                        LlmMessage.serializer(),
                        LlmMessage(role = "model", content = "An error occurred: ${e.message}")
                    ),
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.addMessage(errorMsg)
            } finally {
                chatRepository.setStreamingContent(sessionId, null)
                chatRepository.setGenerating(sessionId, false)
                generationJobs.remove(sessionId)
            }
        }
        generationJobs[sessionId] = job
    }

    fun stopGeneration(sessionId: String) {
        generationJobs[sessionId]?.cancel()
        generationJobs.remove(sessionId)
        chatRepository.setGenerating(sessionId, false)
        chatRepository.setStreamingContent(sessionId, null)
    }
}
