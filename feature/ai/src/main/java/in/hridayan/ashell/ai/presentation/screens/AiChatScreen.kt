package `in`.hridayan.ashell.ai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.presentation.components.bottomsheet.AiSessionOptionsBottomSheet
import `in`.hridayan.ashell.ai.presentation.components.bottomsheet.AiThoughtsBottomSheet
import `in`.hridayan.ashell.ai.presentation.components.chat.AiChatDrawerUI
import `in`.hridayan.ashell.ai.presentation.components.chat.MarkdownMessageContent
import `in`.hridayan.ashell.ai.presentation.components.chat.NewChatPromptSuggestions
import `in`.hridayan.ashell.ai.presentation.components.chat.NewChatWelcomeUI
import `in`.hridayan.ashell.ai.presentation.components.chat.PermissionPromptCard
import `in`.hridayan.ashell.ai.presentation.components.chat.PromptInputField
import `in`.hridayan.ashell.ai.presentation.components.chat.RunningTasks
import `in`.hridayan.ashell.ai.presentation.components.dialog.RenameAiSessionDialog
import `in`.hridayan.ashell.ai.presentation.components.loadingindicator.BouncyDotsLoadingIndicator
import `in`.hridayan.ashell.ai.presentation.model.ChatUiItem
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiChatViewModel
import `in`.hridayan.ashell.core.common.domain.model.SharedTextHolder
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.ApiKeyRequiredDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.hideKeyboard
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val uiState by viewModel.uiState.collectAsState()
    val uiItems by viewModel.uiItems.collectAsState()

    var selectedThoughtGroupId by remember { mutableStateOf<String?>(null) }
    var selectedSessionForOptions by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var showRenameDialogForSession by remember { mutableStateOf<ChatSessionEntity?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    LaunchedEffect(uiState.permissionPrompt != null) {
        if (uiState.permissionPrompt != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            hideKeyboard(context)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AiChatDrawerUI(
                drawerState = drawerState,
                uiState = uiState,
                onClickSession = { selectedSessionId ->
                    viewModel.onSessionSelected(selectedSessionId)
                    scope.launch { drawerState.close() }
                },
                onLongClickSession = {
                    selectedSessionForOptions = it
                }
            )
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
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 30.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiItems.isEmpty()) {
                        NewChatPromptSuggestions(
                            onClickPrompt = { prompt -> viewModel.sendMessage(prompt) }
                        )
                    }

                    if (uiState.runningTasks.isNotEmpty() || uiState.permissionPrompt != null) {
                        CustomCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (uiState.runningTasks.isNotEmpty()) {
                                    uiState.runningTasks.forEach { task ->
                                        RunningTasks(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            taskName = task.name,
                                            onClickStop = withHaptic {
                                                viewModel.cancelRunningTask(
                                                    task.taskId
                                                )
                                            }
                                        )
                                    }
                                }

                                if (uiState.permissionPrompt != null) {
                                    if (uiState.runningTasks.isNotEmpty()) {
                                        HorizontalDivider()
                                    }
                                    val prompt = uiState.permissionPrompt!!
                                    PermissionPromptCard(
                                        modifier = Modifier.padding(10.dp),
                                        prompt = prompt
                                    )
                                }
                            }
                        }
                    }

                    var promptInputText by remember { mutableStateOf("") }

                    PromptInputField(
                        modifier = Modifier.fillMaxWidth(),
                        isGenerating = uiState.isGenerating,
                        onClickTrailingButton = withHaptic {
                            if (uiState.isGenerating) {
                                viewModel.stopGeneration()
                            } else if (promptInputText.isNotBlank()) {
                                hideKeyboard(context)
                                viewModel.sendMessage(promptInputText)
                                promptInputText = ""
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        },
                        textFieldValue = promptInputText,
                        onValueChange = { promptInputText = it },
                    )
                }
            }
        ) { innerPadding ->

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 15.dp),
                contentPadding = innerPadding,
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
                    when (val item = reversedUiItems[index]) {
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
                                                val previousRoute =
                                                    navController.previousBackStackEntry?.destination

                                                val isPrevRouteHomeScreen =
                                                    previousRoute?.hasRoute<NavRoutes.HomeScreen>() == true

                                                if (isPrevRouteHomeScreen) {
                                                    SharedTextHolder.text = command
                                                    navController.navigate(NavRoutes.LocalAdbScreen)
                                                } else {
                                                    navController.previousBackStackEntry
                                                        ?.savedStateHandle
                                                        ?.set("suggestedCommand", command)
                                                    navController.popBackStack()
                                                }
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
                                            val previousRoute =
                                                navController.previousBackStackEntry?.destination?.route
                                            val isPreviousShell =
                                                previousRoute?.contains("AdbScreen") == true || previousRoute?.contains(
                                                    "FastbootScreen"
                                                ) == true

                                            if (isPreviousShell) {
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("suggestedCommand", command)
                                                navController.popBackStack()
                                            } else {
                                                SharedTextHolder.text = command
                                                navController.navigate(NavRoutes.LocalAdbScreen)
                                            }
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

