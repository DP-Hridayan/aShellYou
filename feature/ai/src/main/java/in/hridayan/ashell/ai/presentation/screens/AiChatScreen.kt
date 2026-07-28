package `in`.hridayan.ashell.ai.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxThemes
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.presentation.components.animatedcomposable.AnimatedStopIcon
import `in`.hridayan.ashell.ai.presentation.components.bottomsheet.AiSessionOptionsBottomSheet
import `in`.hridayan.ashell.ai.presentation.components.bottomsheet.AiThoughtsBottomSheet
import `in`.hridayan.ashell.ai.presentation.components.dialog.RenameAiSessionDialog
import `in`.hridayan.ashell.ai.presentation.components.loadingindicator.BouncyDotsLoadingIndicator
import `in`.hridayan.ashell.ai.presentation.model.ChatUiItem
import `in`.hridayan.ashell.ai.presentation.model.MessageComponent
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiChatViewModel
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.animatedcomposables.AnimatedAdbIcon
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.ApiKeyRequiredDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.hideKeyboard
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiItems by viewModel.uiItems.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedThoughtGroupId by remember { mutableStateOf<String?>(null) }
    var selectedSessionForOptions by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var showRenameDialogForSession by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var inputText by remember { mutableStateOf("") }

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.permissionPrompt != null) {
        if (uiState.permissionPrompt != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.ai_chat_sessions),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedButton(
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .fillMaxWidth(),
                    onClick = withHaptic {
                        viewModel.onNewChat()
                        scope.launch { drawerState.close() }
                    }
                ) {
                    Icon(
                        modifier = Modifier.padding(vertical = 5.dp),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        modifier = Modifier.padding(vertical = 5.dp),
                        text = stringResource(R.string.ai_chat_new_chat),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn {
                    items(uiState.sessions) { session ->
                        @OptIn(ExperimentalFoundationApi::class)
                        Surface(
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(50))
                                .combinedClickable(
                                    onClick = withHaptic {
                                        viewModel.onSessionSelected(session.id)
                                        scope.launch { drawerState.close() }
                                    },
                                    onLongClick = withHaptic(HapticFeedbackType.LongPress) {
                                        selectedSessionForOptions = session
                                    }
                                ),
                            shape = CircleShape,
                            color = if (session.id == uiState.currentSessionId) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.run {
                                if (session.id == uiState.currentSessionId) onSecondaryContainer else onSurfaceVariant
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    session.title,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val isGeneratingOrRunning =
                                    uiState.generatingSessionIds.contains(session.id) ||
                                            uiState.runningTasks.any { it.sessionId == session.id }

                                if (isGeneratingOrRunning) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AnimatedStopIcon(modifier = Modifier.size(16.dp))
                                } else if (session.isPinned) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        val currentSession =
                            uiState.sessions.find { it.id == uiState.currentSessionId }
                        val topBarTitle = currentSession?.title?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.adb_agent)
                        Text(
                            text = topBarTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = withHaptic { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 15.dp),
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                    reverseLayout = true,
                ) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )
                    }

                    val reversedUiItems = uiItems.reversed()

                    if (reversedUiItems.isEmpty()) {
                        item {
                            NewChatWelcomeUI(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(horizontal = 24.dp)
                            )
                        }
                    }

                    items(count = reversedUiItems.size, key = { reversedUiItems[it].id }) { index ->
                        val item = reversedUiItems[index]
                        when (item) {
                            is ChatUiItem.UserMessage -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 48.dp, top = 15.dp, bottom = 15.dp)
                                        .animateItem(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            MarkdownMessageContent(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                                content = item.content,
                                                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                onUseCommand = { command ->
                                                    navController.previousBackStackEntry
                                                        ?.savedStateHandle
                                                        ?.set("suggestedCommand", command)
                                                    navController.popBackStack()
                                                }
                                            )
                                        }

                                        if (item.isOrphaned) {
                                            IconButton(
                                                onClick = withHaptic {
                                                    viewModel.retryPrompt(item.id, item.content)
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Retry",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is ChatUiItem.LoadingDots -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 48.dp)
                                        .animateItem(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BouncyDotsLoadingIndicator(
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            is ChatUiItem.ThoughtGroup -> {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .animateItem(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    CustomCard(
                                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                            selectedThoughtGroupId = item.id
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        ),
                                        shape = CustomCardShape(50)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = stringResource(R.string.ai_chat_thoughts),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            is ChatUiItem.ModelMessage -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .animateItem(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column {
                                        MarkdownMessageContent(
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            content = item.content,
                                            textColor = MaterialTheme.colorScheme.onSurface,
                                            onUseCommand = { command ->
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("suggestedCommand", command)
                                                navController.popBackStack()
                                            }
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            IconButton(
                                                onClick = withHaptic {
                                                    ClipboardUtils.copyToClipboard(
                                                        item.content,
                                                        context
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = withHaptic {
                                                    val promptText = reversedUiItems.subList(
                                                        index + 1,
                                                        reversedUiItems.size
                                                    )
                                                        .firstOrNull { it is ChatUiItem.UserMessage }
                                                        ?.let { (it as ChatUiItem.UserMessage).content }
                                                    if (promptText != null) {
                                                        // Note: We don't delete the prompt since we didn't pass the prompt ID, 
                                                        // but we just resend the text. 
                                                        viewModel.sendMessage(promptText)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Retry",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.runningTasks.isNotEmpty() || uiState.permissionPrompt != null) {
                    CustomCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (uiState.runningTasks.isNotEmpty()) {
                                uiState.runningTasks.forEach { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.ai_chat_running) + task.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = withHaptic { viewModel.cancelRunningTask(task.taskId) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Stop,
                                                contentDescription = "Stop Task"
                                            )
                                        }
                                    }
                                }
                            }

                            if (uiState.permissionPrompt != null) {
                                if (uiState.runningTasks.isNotEmpty()) {
                                    HorizontalDivider()
                                }
                                val prompt = uiState.permissionPrompt!!
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = stringResource(R.string.ai_chat_allow_run_command),
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    Text(
                                        text = prompt.command,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        TextButton(onClick = prompt.onDeny) {
                                            Text(stringResource(R.string.ai_chat_deny))
                                        }

                                        TextButton(onClick = prompt.onAllow) {
                                            Text(stringResource(R.string.ai_chat_allow))
                                        }

                                        Button(onClick = prompt.onAlwaysAllowExact) {
                                            Text(stringResource(R.string.always_allow_exact))
                                        }

                                        if (prompt.baseCommand.isNotBlank()) {
                                            Button(onClick = prompt.onAlwaysAllowBase) {
                                                Text(stringResource(R.string.always_allow_base) + " (${prompt.baseCommand})")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiItems.isEmpty()) {
                    NewChatPromptSuggestions(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                        onClickPrompt = { prompt -> viewModel.sendMessage(prompt) }
                    )
                }

                // Input text is a floating card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = withHaptic {}
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.ic_help),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.message_adb_agent),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                        )

                        IconButton(
                            onClick = withHaptic {
                                if (uiState.isGenerating) {
                                    viewModel.stopGeneration()
                                } else if (inputText.isNotBlank()) {
                                    hideKeyboard(context)
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                    scope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            }
                        ) {
                            if (uiState.isGenerating) {
                                AnimatedStopIcon()
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val selectedGroup = uiItems.find { it.id == selectedThoughtGroupId } as? ChatUiItem.ThoughtGroup
    selectedGroup?.let { group ->

        AiThoughtsBottomSheet(
            onDismiss = { selectedThoughtGroupId = null },
            thoughts = group.thoughts,
            isGenerating = group.isGenerating
        )
    }

    selectedSessionForOptions?.let { session ->

        AiSessionOptionsBottomSheet(
            onDismiss = { selectedSessionForOptions = null },
            session = session,
            onPin = {
                viewModel.togglePinSession(session)
                selectedSessionForOptions = null
            },
            onRename = {
                showRenameDialogForSession = session
                selectedSessionForOptions = null
            },
            onDelete = {
                viewModel.deleteSession(session.id)
                selectedSessionForOptions = null
            })
    }

    showRenameDialogForSession?.let { session ->

        RenameAiSessionDialog(
            onDismiss = { showRenameDialogForSession = null },
            onConfirm = { newTitle ->
                viewModel.renameSession(session.id, newTitle)
                showRenameDialogForSession = null
            },
            session = session
        )
    }

    val showApiKeyRequiredDialog by viewModel.showApiKeyRequiredDialog.collectAsState()
    if (showApiKeyRequiredDialog) {
        ApiKeyRequiredDialog(
            onDismiss = { viewModel.dismissApiKeyRequiredDialog() },
            onConfirm = {
                viewModel.dismissApiKeyRequiredDialog()
                navController.navigate(NavRoutes.CloudModelsScreen)
            }
        )
    }
}

@Composable
private fun NewChatWelcomeUI(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedAdbIcon(
            modifier = Modifier.size(100.dp),
            headColor = MaterialTheme.colorScheme.tertiary,
            eyeColor = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.ai_new_chat_welcome_msg),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun NewChatPromptSuggestions(
    modifier: Modifier = Modifier,
    onClickPrompt: (String) -> Unit
) {
    val prompts = listOf(
        stringResource(R.string.ai_prompt_suggestion_1),
        stringResource(R.string.ai_prompt_suggestion_2),
        stringResource(R.string.ai_prompt_suggestion_3)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        prompts.forEach { prompt ->
            OutlinedCard(
                modifier = Modifier,
                shape = RoundedCornerShape(50),
                onClick = withHaptic { onClickPrompt(prompt) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = prompt,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MarkdownMessageContent(
    modifier: Modifier = Modifier,
    textColor: Color,
    content: String,
    onUseCommand: (String) -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val res = LocalResources.current
    val context = LocalContext.current
    val components = remember(content) { viewModel.parseMarkdown(content) }

    Column(modifier = modifier) {
        components.forEach { component ->
            when (component) {
                is MessageComponent.Text -> {
                    SelectionContainer {
                        Markdown(
                            modifier = Modifier.wrapContentWidth(),
                            content = component.text,
                            colors = markdownColor(text = textColor),
                        )
                    }
                }

                is MessageComponent.CodeBlock -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = component.language.ifBlank { "code" },
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                Row {
                                    TextButton(onClick = withHaptic {
                                        ClipboardUtils.copyToClipboard(
                                            text = AnnotatedString(component.code).text,
                                            context = context
                                        )

                                        showToast(
                                            context,
                                            res.getString(R.string.copied_to_clipboard)
                                        )
                                    }) {
                                        Text(stringResource(R.string.copy))
                                    }

                                    if (isShellLanguage(component.language)) {
                                        TextButton(onClick = withHaptic { onUseCommand(component.code) }) {
                                            Text(stringResource(R.string.ai_chat_use))
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            val isDarkTheme = LocalDarkMode.current

                            val highlights =
                                remember(component.code, component.language, isDarkTheme) {
                                    try {
                                        Highlights.Builder()
                                            .code(component.code)
                                            .theme(
                                                SyntaxThemes.default(isDarkTheme)
                                            )
                                            .build()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                            val annotatedString = remember(highlights, component.code) {
                                if (highlights != null) {
                                    buildAnnotatedString {
                                        append(component.code)
                                        highlights.getHighlights().forEach { highlight ->
                                            when (highlight) {
                                                is ColorHighlight -> {
                                                    addStyle(
                                                        style = SpanStyle(color = Color(highlight.rgb or 0xFF000000.toInt())),
                                                        start = highlight.location.start,
                                                        end = highlight.location.end
                                                    )
                                                }

                                                is BoldHighlight -> {
                                                    addStyle(
                                                        style = SpanStyle(fontWeight = FontWeight.Bold),
                                                        start = highlight.location.start,
                                                        end = highlight.location.end
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    AnnotatedString(component.code)
                                }
                            }

                            SelectionContainer {
                                Text(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isShellLanguage(text: String): Boolean {
    val shellLanguages = listOf(
        "sh",
        "bash",
        "shell",
        "cmd",
        "powershell",
        "zsh",
        ""
    )
    return text.lowercase().trim() in shellLanguages
}
