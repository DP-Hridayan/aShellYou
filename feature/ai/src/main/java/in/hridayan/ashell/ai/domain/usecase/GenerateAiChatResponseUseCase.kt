package `in`.hridayan.ashell.ai.domain.usecase

import android.util.Log
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.tool.ToolRegistry
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ModelFallbackExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private const val TAG = "AiChatResponse"
private const val ROLE_USER = "user"
private const val DEFAULT_SESSION_TITLE = "New Chat"
private const val TITLE_CONTEXT_MESSAGES = 3
private val TITLE_REFRESH_TURNS = setOf(1, 4, 10)

class GenerateAiChatResponseUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val toolRegistry: ToolRegistry,
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val modelCatalog: LlmModelCatalog,
    private val fallbackExecutor: ModelFallbackExecutor,
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
    private val systemPromptBuilder: ChatSystemPromptBuilder,
    private val toolDispatcher: ChatToolDispatcher,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend operator fun invoke(sessionId: String) {
        val session = openSession() ?: return
        val systemPrompt = systemPromptBuilder.build()
        var needsTitleUpdate = shouldGenerateTitle(sessionId)

        while (true) {
            val response = requestTurn(sessionId, session, systemPrompt)
            persist(sessionId, response)

            if (response.toolCalls.isEmpty()) {
                if (needsTitleUpdate && response.content.isNotBlank()) {
                    needsTitleUpdate = false
                    scheduleTitleGeneration(sessionId, session)
                }
                return
            }

            persist(sessionId, executeTools(sessionId, response))
        }
    }

    private suspend fun requestTurn(
        sessionId: String,
        session: ChatSession,
        systemPrompt: String,
    ): LlmMessage {
        val history = loadHistory(sessionId)
        val tools = toolRegistry.getEnabledTools()

        return fallbackExecutor.execute(session.provider, session.models) { model ->
            chatRepository.setStreamingContent(sessionId, "")
            withContext(Dispatchers.IO) {
                session.client.completeWithHistoryStream(
                    model = model,
                    systemPrompt = systemPrompt,
                    history = history,
                    apiKey = session.apiKey,
                    tools = tools,
                ) { chunk ->
                    val current = chatRepository.streamingContents.value[sessionId].orEmpty()
                    chatRepository.setStreamingContent(sessionId, current + chunk)
                }
            }
        }
    }

    private suspend fun executeTools(sessionId: String, response: LlmMessage): LlmMessage =
        LlmMessage(
            role = ROLE_USER,
            content = "",
            toolResponses = toolDispatcher.dispatch(sessionId, response.toolCalls),
            providerId = response.providerId,
        )

    private suspend fun openSession(): ChatSession? {
        val provider = getActiveLlmProvider() ?: return null
        val client = clients[provider] ?: return null
        val apiKey = apiKeyRepository.getKey(provider)?.takeIf { it.isNotBlank() } ?: return null
        val models = modelCatalog.chain(provider, ModelTier.LITE, toolsRequired = true)

        return models.takeIf { it.isNotEmpty() }
            ?.let { ChatSession(provider, client, apiKey, it) }
    }

    private suspend fun loadHistory(sessionId: String): List<LlmMessage> =
        chatRepository.getMessagesForSessionSync(sessionId).map { entity ->
            try {
                json.decodeFromString<LlmMessage>(entity.rawContent)
            } catch (e: Exception) {
                Log.w(TAG, "Unreadable message ${entity.id}, replaying it as plain text", e)
                LlmMessage(role = entity.role, content = entity.rawContent)
            }
        }

    private suspend fun persist(sessionId: String, message: LlmMessage) {
        chatRepository.addMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = message.role,
                rawContent = json.encodeToString(LlmMessage.serializer(), message),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun shouldGenerateTitle(sessionId: String): Boolean {
        val session = chatRepository.getSessionByIdSync(sessionId)
        if (session?.isUserRenamed == true) return false

        val userMessageCount = chatRepository.getMessagesForSessionSync(sessionId)
            .count { it.role == ROLE_USER }

        return session?.title.isNullOrBlank() ||
            session?.title.equals(DEFAULT_SESSION_TITLE, ignoreCase = true) ||
            userMessageCount in TITLE_REFRESH_TURNS
    }

    private suspend fun scheduleTitleGeneration(sessionId: String, session: ChatSession) {
        val contextSummary = loadHistory(sessionId)
            .filter { it.role == ROLE_USER }
            .takeLast(TITLE_CONTEXT_MESSAGES)
            .joinToString(" | ") { it.content }
        val model = session.models.first()

        CoroutineScope(Dispatchers.IO).launch {
            generateTitle(sessionId, contextSummary, session, model)
        }
    }

    private suspend fun generateTitle(
        sessionId: String,
        contextSummary: String,
        session: ChatSession,
        model: String,
    ) {
        try {
            if (chatRepository.getSessionByIdSync(sessionId)?.isUserRenamed == true) return

            val localeName = Locale.getDefault().displayName
            val prompt = "Generate a short, concise title (max 5 words) for a conversation " +
                "about: \"$contextSummary\". Do not include quotes. " +
                "CRITICAL: The title MUST be generated in the language: $localeName."

            val title = session.client
                .complete(model, TITLE_SYSTEM_PROMPT, prompt, session.apiKey)
                .trim()
                .removeSurrounding("\"")

            if (title.isNotBlank()) chatRepository.updateSessionTitle(sessionId, title)
        } catch (e: CloudNetworkException) {
            Log.w(TAG, "Title generation failed; the session keeps its current name", e)
        } catch (e: Exception) {
            Log.w(TAG, "Title generation failed; the session keeps its current name", e)
        }
    }

    private data class ChatSession(
        val provider: LlmProvider,
        val client: LlmProviderClient,
        val apiKey: String,
        val models: List<String>,
    )

    private companion object {
        const val TITLE_SYSTEM_PROMPT = "You are a helpful assistant."
    }
}
