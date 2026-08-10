package `in`.hridayan.ashell.ai.presentation.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.domain.manager.ChatSessionManager
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.tool.CommandExecutionManager
import `in`.hridayan.ashell.ai.presentation.model.AiChatUiState
import `in`.hridayan.ashell.ai.presentation.model.ChatUiItem
import `in`.hridayan.ashell.ai.presentation.model.MessageComponent
import `in`.hridayan.ashell.ai.presentation.model.PermissionPrompt
import `in`.hridayan.ashell.ai.presentation.model.RunningTaskUiModel
import `in`.hridayan.ashell.ai.presentation.model.Thought
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val commandExecutionManager: CommandExecutionManager,
    private val chatSessionManager: ChatSessionManager,
    private val apiKeyRepository: ApiKeyRepository
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _showApiKeyRequiredDialog = MutableStateFlow(false)
    val showApiKeyRequiredDialog = _showApiKeyRequiredDialog.asStateFlow()

    fun dismissApiKeyRequiredDialog() {
        _showApiKeyRequiredDialog.value = false
    }

    private val _currentSessionId = MutableStateFlow(chatSessionManager.activeSessionId)
    private val _sessionSearchQuery = MutableStateFlow(TextFieldValue(""))
    val sessionSearchQuery = _sessionSearchQuery.asStateFlow()

    val chatSessions = combine(
        chatRepository.getAllSessions(),
        _sessionSearchQuery
    ) { sessions, query ->
        if (query.text.isBlank()) {
            sessions
        } else {
            sessions.filter { it.title.contains(query.text, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSessionSearchQueryChange(query: TextFieldValue) {
        _sessionSearchQuery.value = query
    }

    val uiState: StateFlow<AiChatUiState> = combine(
        chatSessions,
        _currentSessionId,
        chatRepository.activeGeneratingSessions,
        commandExecutionManager.permissionRequest,
        commandExecutionManager.runningTasks
    ) { sessions, currentSessionId, activeGeneratingSessions, permissionReq, runningTasks ->
        AiChatUiState(
            sessions = sessions,
            currentSessionId = currentSessionId,
            isGenerating = currentSessionId != null && activeGeneratingSessions.contains(
                currentSessionId
            ),
            generatingSessionIds = activeGeneratingSessions,
            permissionPrompt = permissionReq?.let { req ->
                PermissionPrompt(
                    command = req.command,
                    baseCommand = req.baseCommand,
                    onAllow = {
                        handlePermission(
                            req.command,
                            isAllowed = true,
                            alwaysAllowExact = false,
                            alwaysAllowBase = false
                        )
                    },
                    onAlwaysAllowExact = {
                        handlePermission(
                            req.command,
                            isAllowed = true,
                            alwaysAllowExact = true,
                            alwaysAllowBase = false
                        )
                    },
                    onAlwaysAllowBase = {
                        handlePermission(
                            req.command,
                            isAllowed = true,
                            alwaysAllowExact = false,
                            alwaysAllowBase = true
                        )
                    },
                    onDeny = {
                        handlePermission(
                            req.command,
                            isAllowed = false,
                            alwaysAllowExact = false,
                            alwaysAllowBase = false
                        )
                    }
                )
            },
            runningTasks = runningTasks.map { task ->
                RunningTaskUiModel(
                    taskId = task.id,
                    name = task.name,
                    isRunning = true,
                    sessionId = task.sessionId
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiChatUiState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiItems: StateFlow<List<ChatUiItem>> =
        combine(
            _currentSessionId.flatMapLatest { sessionId ->
                if (sessionId != null) {
                    chatRepository.getMessagesForSession(sessionId)
                } else {
                    flowOf(emptyList())
                }
            },
            combine(_currentSessionId, chatRepository.streamingContents) { id, map ->
                if (id != null) map[id] else null
            }
        ) { messages, streamingText ->
            val items = mutableListOf<ChatUiItem>()
            val turnThoughts = mutableListOf<Thought>()
            val turnModelMessages = mutableListOf<ChatUiItem.ModelMessage>()
            var currentTurnId = "initial_turn"

            val isCurrentlyGenerating =
                uiState.value.generatingSessionIds.contains(_currentSessionId.value)

            messages.forEach { msg ->
                val llmMessage = try {
                    json.decodeFromString<LlmMessage>(msg.rawContent)
                } catch (e: Exception) {
                    null
                }

                val isUserPrompt =
                    msg.role == "user" && (llmMessage == null || llmMessage.toolResponse == null)

                if (isUserPrompt) {
                    if (turnThoughts.isNotEmpty()) {
                        items.add(
                            ChatUiItem.ThoughtGroup(
                                currentTurnId + "_thoughts",
                                turnThoughts.toList(),
                                isGenerating = false
                            )
                        )
                        turnThoughts.clear()
                    }
                    items.addAll(turnModelMessages)
                    turnModelMessages.clear()

                    currentTurnId = msg.id

                    val msgIndex = messages.indexOf(msg)
                    val nextMsg = messages.getOrNull(msgIndex + 1)
                    val isLast = msgIndex == messages.lastIndex

                    val isOrphaned = if (isLast) {
                        !isCurrentlyGenerating && streamingText.isNullOrEmpty()
                    } else {
                        // Next message is user if it has role "user" and NO tool response!
                        val nextLlmMsg = try {
                            nextMsg?.let { json.decodeFromString<LlmMessage>(it.rawContent) }
                        } catch (e: Exception) {
                            null
                        }
                        nextMsg?.role == "user" && (nextLlmMsg == null || nextLlmMsg.toolResponse == null)
                    }

                    val textContent = llmMessage?.content ?: msg.rawContent

                    if (!isOrphaned || isLast) {
                        items.add(ChatUiItem.UserMessage(msg.id, textContent, isOrphaned))
                    }
                } else {
                    if (llmMessage == null) {
                        if (msg.rawContent.isNotBlank()) {
                            turnModelMessages.add(ChatUiItem.ModelMessage(msg.id, msg.rawContent))
                        }
                    } else {
                        val toolCall = llmMessage.toolCall
                        val toolRes = llmMessage.toolResponse
                        val textContent = llmMessage.content

                        if (toolCall != null) {
                            val argsStr = toolCall.args?.toString() ?: ""
                            val formattedArgs = try {
                                val parsed = json.parseToJsonElement(argsStr)
                                val prettyJson =
                                    kotlinx.serialization.json.Json { prettyPrint = true }
                                prettyJson.encodeToString(
                                    kotlinx.serialization.json.JsonElement.serializer(),
                                    parsed
                                )
                            } catch (e: Exception) {
                                argsStr
                            }

                            turnThoughts.add(
                                Thought(
                                    "Executing Tool: ${toolCall.name}",
                                    formattedArgs.take(500) + if (formattedArgs.length > 500) "\n\n[...Args truncated for UI]" else ""
                                )
                            )
                        }
                        if (toolRes != null) {
                            val resStr = toolRes.result
                            val lines = resStr.lines()
                            val truncatedLines = if (lines.size > 10) {
                                lines.take(10)
                                    .joinToString("\n") + "\n\n[...Output truncated (${lines.size - 10} more lines)]"
                            } else {
                                resStr
                            }

                            val finalRes = if (truncatedLines.length > 500) {
                                truncatedLines.take(500) + "\n\n[...Output truncated for UI]"
                            } else {
                                truncatedLines
                            }

                            turnThoughts.add(Thought("Result: ${toolRes.name}", finalRes))
                        }

                        if (textContent.isNotBlank()) {
                            turnModelMessages.add(ChatUiItem.ModelMessage(msg.id, textContent))
                        }
                    }
                }
            }

            // Flush final turn
            if (turnThoughts.isNotEmpty()) {
                items.add(
                    ChatUiItem.ThoughtGroup(
                        currentTurnId + "_thoughts",
                        turnThoughts.toList(),
                        isGenerating = isCurrentlyGenerating
                    )
                )
            }
            items.addAll(turnModelMessages)

            if (streamingText != null) {
                var alreadyInDb = false
                val lastMsg = messages.lastOrNull()
                if (lastMsg != null && lastMsg.role == "model") {
                    try {
                        val llmMsg = json.decodeFromString<LlmMessage>(lastMsg.rawContent)
                        if (llmMsg.content == streamingText) {
                            alreadyInDb = true
                        }
                    } catch (e: Exception) {
                        if (lastMsg.rawContent == streamingText) {
                            alreadyInDb = true
                        }
                    }
                }

                if (!alreadyInDb) {
                    if (streamingText.isEmpty()) {
                        items.add(ChatUiItem.LoadingDots())
                    } else {
                        items.add(ChatUiItem.ModelMessage("streaming_content", streamingText))
                    }
                }
            }

            items
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            _currentSessionId.collect {
                chatSessionManager.activeSessionId = it
            }
        }
    }

    fun onNewChat() {
        val newSessionId = UUID.randomUUID().toString()
        viewModelScope.launch {
            chatRepository.createNewSession(newSessionId, "New Chat")
            _currentSessionId.value = newSessionId
        }
    }

    fun onSessionSelected(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    private fun handlePermission(
        command: String,
        isAllowed: Boolean,
        alwaysAllowExact: Boolean,
        alwaysAllowBase: Boolean
    ) {
        viewModelScope.launch {
            commandExecutionManager.handlePermissionResponse(
                command,
                isAllowed,
                alwaysAllowExact,
                alwaysAllowBase
            )
        }
    }

    fun cancelRunningTask(taskId: String) {
        commandExecutionManager.cancelTask(taskId)
    }

    fun stopGeneration() {
        val sessionId = _currentSessionId.value ?: return
        chatSessionManager.stopGeneration(sessionId)
    }

    fun retryPrompt(messageId: String, text: String) {
        viewModelScope.launch {
            // Delete the orphaned message and resend
            chatRepository.deleteMessage(messageId)
            sendMessage(text)
        }
    }

    fun sendMessage(text: String) {
        if (apiKeyRepository.getKey(LlmProvider.Gemini).isNullOrBlank()) {
            _showApiKeyRequiredDialog.value = true
            return
        }
        val sessionId = _currentSessionId.value ?: UUID.randomUUID().toString().also { newId ->
            viewModelScope.launch {
                chatRepository.createNewSession(newId, "New Chat")
                _currentSessionId.value = newId
            }
        }

        chatSessionManager.sendMessage(sessionId) {
            val userLlmMessage = LlmMessage(
                role = "user",
                content = text
            )

            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "user",
                rawContent = json.encodeToString(LlmMessage.serializer(), userLlmMessage),
                timestamp = System.currentTimeMillis()
            )
            chatRepository.addMessage(userMsg)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateSessionTitleByUser(sessionId, newTitle)
        }
    }

    fun togglePinSession(session: `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity) {
        viewModelScope.launch {
            chatRepository.updateSessionPinned(session.id, !session.isPinned)
        }
    }

    fun parseMarkdown(content: String): List<MessageComponent> {
        val components = mutableListOf<MessageComponent>()
        val regex = Regex("```(\\w*)\\n(.*?)```", RegexOption.DOT_MATCHES_ALL)
        var lastIndex = 0
        regex.findAll(content).forEach { matchResult ->
            val textBefore = content.substring(lastIndex, matchResult.range.first)
            if (textBefore.isNotBlank()) {
                components.add(MessageComponent.Text(textBefore.trim()))
            }
            val language = matchResult.groupValues[1]
            val code = matchResult.groupValues[2].trim()
            components.add(MessageComponent.CodeBlock(language, code))
            lastIndex = matchResult.range.last + 1
        }
        val textAfter = content.substring(lastIndex)
        if (textAfter.isNotBlank()) {
            components.add(MessageComponent.Text(textAfter.trim()))
        }
        return components
    }
}
