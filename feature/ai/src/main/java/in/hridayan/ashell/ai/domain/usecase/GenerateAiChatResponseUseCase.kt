package `in`.hridayan.ashell.ai.domain.usecase

import android.util.Log
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.tool.ToolRegistry
import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmToolResponse
import `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.AiConnectionStateProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class GenerateAiChatResponseUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val toolRegistry: ToolRegistry,
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val settingsRepository: SettingsRepository,
    private val otgRepository: OtgRepository,
    private val aiConnectionStateProvider: AiConnectionStateProvider
) {
    private val json = Json {
        ignoreUnknownKeys = true;
        encodeDefaults = true
    }

    suspend operator fun invoke(sessionId: String) {
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        val provider = LlmProvider.fromId(providerId) ?: LlmProvider.Gemini
        val client = clients[provider] ?: return
        val apiKey = apiKeyRepository.getKey(provider) ?: return

        // For chat we can use lite models and fallback; maintain a mutable active list for the entire prompt turn
        val activeModels = AiModelConstants.geminiLiteModels.toMutableList()
        if (activeModels.isEmpty()) return

        val currentModeId =
            settingsRepository.getInt(SettingsKeys.LocalAdbWorkingMode).firstOrNull()
                ?: SettingsKeys.LocalAdbWorkingMode.default

        val localContext = when (currentModeId) {
            LocalAdbWorkingMode.ROOT -> "Root"
            LocalAdbWorkingMode.SHIZUKU -> "Shizuku"
            LocalAdbWorkingMode.TCPIP -> "TCP/IP ADB"
            else -> "Basic"
        }

        val otgContext = if (otgRepository.isConnected()) {
            val device = otgRepository.getAdbConnection()
            "Connected (Max Payload: ${device?.maxData ?: "Unknown"})"
        } else {
            "Not connected"
        }

        val wifiDeviceName = aiConnectionStateProvider.getWifiConnectedDeviceName()
        val wifiContext = if (wifiDeviceName != null) {
            val type =
                if (aiConnectionStateProvider.isWifiOwnDevice()) "Own Device" else "Other Device"
            "Connected to $wifiDeviceName ($type)"
        } else {
            "Not connected"
        }
        val pairedWifiDevices = aiConnectionStateProvider.getWifiPairedDevices()
        val pairedDevicesStr =
            if (pairedWifiDevices.isNotEmpty()) pairedWifiDevices.joinToString(", ") else "None"

        val localeName = Locale.getDefault().displayName
        val systemPrompt =
            "You are a helpful AI shell assistant. You can execute commands on the user's Android device and answer questions. You MUST use your tools when appropriate to execute commands.\n\n" +
                    "CONNECTION CAPABILITIES:\n" +
                    "You can execute commands on different targets by specifying the `target_mode` parameter in your `execute_command` tool (options: LOCAL, WIRELESS, OTG). Defaults to LOCAL if omitted.\n" +
                    "- LOCAL: Currently in $localContext mode.\n" +
                    "- WIRELESS: $wifiContext. Paired devices: $pairedDevicesStr.\n" +
                    "- OTG: $otgContext.\n" +
                    "Always choose the appropriate target based on the user's request. If the user doesn't specify, default to the most capable connected device.\n\n" +
                    "QUICK SETTINGS (QS) TILES RULES:\n" +
                    "1. If the user asks to create a Quick Settings (QS) tile, ALWAYS call `get_qs_tile_slots` first to see which of the 10 fixed slots (1-10) are empty/available.\n" +
                    "2. If the user did not specify an execution mode for the tile, ASK the user which execution mode they prefer (0 for Shizuku [default], 1 for Root) before calling `create_qs_tile`.\n" +
                    "3. Decide whether the tile should be toggleable (`is_toggleable = true` with active/inactive commands) or a simple tap action based on the user's request.\n" +
                    "4. After creating the tile, tell the user which slot number (1-10) it was created in and confirm that the system prompt dialog was triggered to add the tile to their Quick Settings panel.\n\n" +
                    "CRITICAL: You must communicate and provide all your conversational responses and explanations in the user's local language ($localeName). However, all shell commands, ADB commands, tool names, and technical function names must remain strictly in English.\n\n" +
                    "IMPORTANT: When you suggest a command for the user to run themselves, you MUST format it as a markdown code block (e.g., ```bash\\ncommand\\n```). The UI will automatically detect this and render a 'Use' button for the user."

        val currentSession = chatRepository.getSessionByIdSync(sessionId)
        val initialMessages = chatRepository.getMessagesForSessionSync(sessionId)
        val userMessageCount = initialMessages.count { it.role == "user" }
        var needsTitleUpdate = (currentSession?.isUserRenamed != true) && (
                (currentSession?.title?.equals("New Chat", ignoreCase = true) == true) ||
                        (currentSession?.title?.isBlank() == true) ||
                        (userMessageCount == 1) ||
                        (userMessageCount == 4) ||
                        (userMessageCount == 10)
                )

        while (true) {
            val dbMessages = chatRepository.getMessagesForSessionSync(sessionId)
            val history = dbMessages.map { entity ->
                try {
                    json.decodeFromString<LlmMessage>(entity.rawContent)
                } catch (e: Exception) {
                    // Fallback for raw text
                    LlmMessage(role = entity.role, content = entity.rawContent)
                }
            }

            var lastException: Exception? = null
            var response: LlmMessage? = null

            for (model in activeModels.toList()) {
                try {
                    chatRepository.setStreamingContent(sessionId, "")
                    response = withContext(Dispatchers.IO) {
                        client.completeWithHistoryStream(
                            model = model,
                            systemPrompt = systemPrompt,
                            history = history,
                            apiKey = apiKey,
                            tools = toolRegistry.getAllTools()
                        ) { chunk ->
                            val current = chatRepository.streamingContents.value[sessionId] ?: ""
                            chatRepository.setStreamingContent(sessionId, current + chunk)
                        }
                    }
                    break // Success, exit model loop
                } catch (e: CloudNetworkException.RateLimited) {
                    lastException = e
                    activeModels.remove(model)
                    Log.w(
                        "GenerateAiChatResponse",
                        "Model $model rate limited, removing from fallback list for this prompt turn"
                    )
                } catch (e: CloudNetworkException.ServerError) {
                    if (e.code == 429 || e.code >= 500 || e.code == 404) {
                        lastException = e
                        activeModels.remove(model)
                        Log.w(
                            "GenerateAiChatResponse",
                            "Model $model server error ${e.code}, removing from fallback list for this prompt turn"
                        )
                    } else {
                        throw e
                    }
                } catch (e: CloudNetworkException.NetworkError) {
                    lastException = e
                    activeModels.remove(model)
                    Log.w(
                        "GenerateAiChatResponse",
                        "Model $model network/timeout error, removing from fallback list for this prompt turn"
                    )
                } catch (e: CloudNetworkException.ParseError) {
                    lastException = e
                    activeModels.remove(model)
                    Log.w(
                        "GenerateAiChatResponse",
                        "Model $model parse error, removing from fallback list for this prompt turn"
                    )
                } catch (e: CloudNetworkException) {
                    // Fatal errors like Unauthorized
                    throw e
                }
            }

            if (response == null) {
                throw lastException
                    ?: java.lang.IllegalStateException("Failed to generate chat response with all fallback models.")
            }

            // Save model response to DB
            chatRepository.addMessage(
                ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = response.role,
                    rawContent = json.encodeToString(LlmMessage.serializer(), response),
                    timestamp = System.currentTimeMillis()
                )
            )

            val toolCall = response.toolCall
            if (toolCall != null) {
                // Execute tool
                val tool = toolRegistry.getToolByName(toolCall.name)
                val resultText: String = if (tool != null) {
                    try {
                        withContext(
                            `in`.hridayan.ashell.core.common.domain.model.ai.SessionIdContext(
                                sessionId
                            )
                        ) {
                            tool.execute(toolCall.args)
                        }
                    } catch (e: Exception) {
                        e.message ?: "Unknown error"
                    }
                } else {
                    "Tool not found"
                }

                // Truncate to prevent SQLiteBlobTooBigException (CursorWindow limit) and LLM context overflow
                val truncatedResult = if (resultText.length > 50000) {
                    resultText.take(50000) + "\n\n[OUTPUT TRUNCATED: Output exceeded 50,000 characters.]"
                } else {
                    resultText
                }

                val toolResponseMsg = LlmMessage(
                    role = "user",
                    content = "",
                    toolResponse = LlmToolResponse(name = toolCall.name, result = truncatedResult)
                )

                // Save tool response to DB
                chatRepository.addMessage(
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = "user",
                        rawContent = json.encodeToString(LlmMessage.serializer(), toolResponseMsg),
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                // Not a tool call, we are done
                if (needsTitleUpdate && response.content.isNotBlank()) {
                    needsTitleUpdate = false
                    val contextSummary = history.filter { it.role == "user" }
                        .takeLast(3)
                        .joinToString(" | ") { it.content }
                    val titleModel =
                        activeModels.firstOrNull() ?: AiModelConstants.geminiLiteModels.first()
                    CoroutineScope(Dispatchers.IO).launch {
                        generateTitle(
                            sessionId,
                            contextSummary,
                            provider,
                            titleModel,
                            apiKey,
                            localeName
                        )
                    }
                }
                break
            }
        }
    }

    private suspend fun generateTitle(
        sessionId: String,
        contextSummary: String,
        provider: LlmProvider,
        model: String,
        apiKey: String,
        localeName: String
    ) {
        try {
            val current = chatRepository.getSessionByIdSync(sessionId)
            if (current?.isUserRenamed == true) return
            val client = clients[provider] ?: return
            val prompt =
                "Generate a short, concise title (max 5 words) for a conversation about: \"$contextSummary\". Do not include quotes. CRITICAL: The title MUST be generated in the language: $localeName."
            val titleResponse =
                client.complete(model, "You are a helpful assistant.", prompt, apiKey)
            val title = titleResponse.trim().removeSurrounding("\"")
            if (title.isNotBlank()) {
                chatRepository.updateSessionTitle(sessionId, title)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
