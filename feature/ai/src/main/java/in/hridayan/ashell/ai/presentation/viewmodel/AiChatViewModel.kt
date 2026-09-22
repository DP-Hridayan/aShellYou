package `in`.hridayan.ashell.ai.presentation.viewmodel

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatMessageEntity
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.domain.manager.ChatSessionManager
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.tool.CommandExecutionManager
import `in`.hridayan.ashell.ai.presentation.model.AiChatUiState
import `in`.hridayan.ashell.ai.presentation.model.ChatUiItem
import `in`.hridayan.ashell.ai.presentation.model.PermissionPrompt
import `in`.hridayan.ashell.ai.presentation.model.RunningTaskUiModel
import `in`.hridayan.ashell.ai.presentation.model.Thought
import `in`.hridayan.ashell.ai.presentation.util.ThoughtFormatter
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmMessage
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ActiveProviderKeyStatus
import `in`.hridayan.ashell.core.common.domain.usecase.ai.RequireActiveProviderKeyUseCase
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val chatRepository: ChatRepository,
    private val commandExecutionManager: CommandExecutionManager,
    private val chatSessionManager: ChatSessionManager,
    private val requireActiveProviderKey: RequireActiveProviderKeyUseCase
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val thinkingLabel: String by lazy { appContext.getString(R.string.thinking) }

    private val _aiAccessPrompt = MutableStateFlow<ActiveProviderKeyStatus?>(null)
    val aiAccessPrompt: StateFlow<ActiveProviderKeyStatus?> = _aiAccessPrompt.asStateFlow()

    fun dismissAiAccessPrompt() {
        _aiAccessPrompt.value = null
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
                if (sessionId == null) {
                    flowOf(emptyList())
                } else {
                    chatRepository.getMessagesForSession(sessionId).map { messages ->
                        messages.map { msg ->
                            val llmMessage = try {
                                json.decodeFromString<LlmMessage>(msg.rawContent)
                            } catch (e: Exception) {
                                null
                            }
                            msg to llmMessage
                        }
                    }.flowOn(Dispatchers.Default)
                }
            },
            combine(_currentSessionId, chatRepository.streamingContents) { id, map ->
                if (id != null) map[id] else null
            }
        ) { messagesWithParsed, streamingText ->
            val items = mutableListOf<ChatUiItem>()
            val turnThoughts = mutableListOf<Thought>()
            val turnModelMessages = mutableListOf<ChatUiItem.ModelMessage>()
            var currentTurnId = "initial_turn"

            val isCurrentlyGenerating =
                uiState.value.generatingSessionIds.contains(_currentSessionId.value)

            messagesWithParsed.forEachIndexed { msgIndex, (msg, llmMessage) ->
                val isUserPrompt =
                    msg.role == "user" && (llmMessage == null || llmMessage.toolResponses.isEmpty())

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

                    val nextMsgPair = messagesWithParsed.getOrNull(msgIndex + 1)
                    val isLast = msgIndex == messagesWithParsed.lastIndex

                    val isOrphaned = if (isLast) {
                        !isCurrentlyGenerating && streamingText.isNullOrEmpty()
                    } else {
                        val nextMsg = nextMsgPair?.first
                        val nextLlmMsg = nextMsgPair?.second
                        nextMsg?.role == "user" && (nextLlmMsg == null || nextLlmMsg.toolResponses.isEmpty())
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
                        // A reasoning-only reply would otherwise render as an empty bubble.
                        val textContent = llmMessage.content.ifBlank {
                            if (llmMessage.toolCalls.isEmpty()) {
                                llmMessage.reasoning.orEmpty()
                            } else {
                                ""
                            }
                        }

                        llmMessage.reasoning
                            ?.takeIf { it.isNotBlank() && it != textContent }
                            ?.let { reasoning ->
                                turnThoughts.add(
                                    Thought(
                                        thinkingLabel,
                                        ThoughtFormatter.formatResult(reasoning)
                                    )
                                )
                            }

                        llmMessage.toolCalls.forEach { toolCall ->
                            turnThoughts.add(
                                Thought(
                                    "Executing Tool: ${toolCall.name}",
                                    ThoughtFormatter.formatArgs(toolCall.args?.toString().orEmpty())
                                )
                            )
                        }

                        llmMessage.toolResponses.forEach { toolRes ->
                            turnThoughts.add(
                                Thought("Result: ${toolRes.name}", ThoughtFormatter.formatResult(toolRes.result))
                            )
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
                val lastMsgPair = messagesWithParsed.lastOrNull()
                if (lastMsgPair != null && lastMsgPair.first.role == "model") {
                    val llmMsg = lastMsgPair.second
                    if (llmMsg != null) {
                        if (llmMsg.content == streamingText) {
                            alreadyInDb = true
                        }
                    } else {
                        if (lastMsgPair.first.rawContent == streamingText) {
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
        }.flowOn(Dispatchers.Default)
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
        viewModelScope.launch {
            val sessions = chatRepository.getAllSessions().first()
            val latestSession = sessions.maxByOrNull { it.createdAt }
            var reuseSessionId: String? = null

            if (latestSession != null) {
                val messages = chatRepository.getMessagesForSessionSync(latestSession.id)
                if (messages.isEmpty()) {
                    reuseSessionId = latestSession.id
                }
            }

            if (reuseSessionId != null) {
                _currentSessionId.value = reuseSessionId
            } else {
                val newSessionId = UUID.randomUUID().toString()
                chatRepository.createNewSession(newSessionId, "New Chat")
                _currentSessionId.value = newSessionId
            }
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
        viewModelScope.launch {
            if (!hasUsableProvider()) return@launch
            dispatchMessage(text)
        }
    }

    private fun dispatchMessage(text: String) {
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
            chatSessionManager.stopGeneration(sessionId)
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

    fun togglePinSession(session: ChatSessionEntity) {
        viewModelScope.launch {
            chatRepository.updateSessionPinned(session.id, !session.isPinned)
        }
    }

    private suspend fun hasUsableProvider(): Boolean {
        val status = requireActiveProviderKey()
        if (status !is ActiveProviderKeyStatus.Allowed) _aiAccessPrompt.value = status
        return status is ActiveProviderKeyStatus.Allowed
    }
}
