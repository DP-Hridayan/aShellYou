@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class
)

package `in`.hridayan.ashell.shell.common.presentation.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.domain.model.ScrollDirection
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scrollbar.VerticalScrollbar
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.utils.disableKeyboard
import `in`.hridayan.ashell.core.presentation.utils.hideKeyboard
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.findActivity
import `in`.hridayan.ashell.core.utils.saveToFileFlow
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.common.presentation.components.button.UtilityButtonGroup
import `in`.hridayan.ashell.shell.common.presentation.components.card.SuggestionCard
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.BookmarkDialog
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.BookmarksSortDialog
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.ClearOutputConfirmationDialog
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.DeleteBookmarksDialog
import `in`.hridayan.ashell.shell.common.presentation.components.dialog.FileSavedDialog
import `in`.hridayan.ashell.shell.common.presentation.components.icon.AnimatedStopIcon
import `in`.hridayan.ashell.shell.common.presentation.model.CommandResult
import `in`.hridayan.ashell.shell.common.presentation.model.ShellScreenState
import `in`.hridayan.ashell.shell.common.presentation.model.ShellState
import `in`.hridayan.ashell.shell.common.presentation.util.highlightQueryText
import `in`.hridayan.ashell.shell.common.presentation.util.rememberScrollDirection
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.BookmarkViewModel
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BaseShellScreen(
    modifier: Modifier = Modifier,
    runCommandIfPermissionGranted: () -> Unit = {},
    modeButtonOnClick: () -> Unit = {},
    modeButtonText: String = stringResource(R.string.mode),
    shellViewModel: ShellViewModel = hiltViewModel(),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    extraButtonContent: @Composable (() -> Unit)? = null,
    extraContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scrollDirection = rememberScrollDirection(listState)
    val states by shellViewModel.states.collectAsState()
    val isKeyboardVisible = isKeyboardVisible().value
    val searchOutputResult by shellViewModel.filteredOutput.collectAsState()
    val disableSoftKeyboard = LocalSettings.current.disableSoftKeyboard
    val bookmarkCount = bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)
    val lastSavedFileUri = LocalSettings.current.lastSavedFileUri
    val textFieldFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var historyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isOutputFullscreen by rememberSaveable { mutableStateOf(false) }
    var restoredScrollIndex by rememberSaveable { mutableIntStateOf(-1) }
    val dialogManager = LocalDialogManager.current

    LaunchedEffect(disableSoftKeyboard) {
        disableKeyboard(context, disableSoftKeyboard)
    }


    val actionFabIcon: @Composable () -> Unit = {
        when (states.shellState) {
            is ShellState.Busy -> AnimatedStopIcon()
            is ShellState.Free -> Icon(
                painter = painterResource(R.drawable.ic_help),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            else -> Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    val actionFabOnClick: () -> Unit = withHaptic {
        when (states.shellState) {
            is ShellState.InputQuery -> {
                coroutineScope.launch {
                    if (isKeyboardVisible) hideKeyboard(context)
                    awaitFrame()
                    runCommandIfPermissionGranted()
                }
            }

            is ShellState.Busy -> {
                shellViewModel.stopCommand()
            }

            is ShellState.Free -> {
                navController.navigate(NavRoutes.CommandExamplesScreen)
            }
        }
    }

    val handleBookmarkButtonClick: () -> Unit = {
        if (bookmarkCount.value == 0) showToast(
            context,
            context.getString(R.string.no_bookmarks)
        ) else dialogManager.show(DialogKey.Shell.Bookmark)
    }

    val handleHistoryButtonClick: () -> Unit = {
        if (states.cmdHistory.isEmpty()) showToast(
            context,
            context.getString(R.string.no_history)
        ) else historyMenuExpanded = true
    }

    val handleClearOutput: () -> Unit = {
        shellViewModel.clearOutput()
        focusManager.clearFocus()
    }

    val handleSaveButtonClick: (success: Boolean) -> Unit = { success ->
        if (success) dialogManager.show(DialogKey.Shell.FileSaved)
    }

    val handleSavedFileOpen: () -> Unit = {
        try {
            val uri = lastSavedFileUri.toUri()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(intent, "Open text file")
            )
        } catch (e: Exception) {
            showToast(context, context.getString(R.string.file_not_found))
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        floatingActionButton = {
            // Hide FABs in fullscreen mode
            AnimatedVisibility(
                visible = !isOutputFullscreen,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                // Scroll button visibility with 3-second hide delay
                var lastScrollDirection by remember { mutableStateOf(ScrollDirection.NONE) }
                var showScrollButton by remember { mutableStateOf(false) }
                val isNotBusy = states.shellState !is ShellState.Busy
                val canScroll = listState.canScrollBackward || listState.canScrollForward

                // Track scroll direction
                LaunchedEffect(scrollDirection) {
                    if (scrollDirection != ScrollDirection.NONE && isNotBusy && canScroll) {
                        lastScrollDirection = scrollDirection
                        showScrollButton = true
                    }
                }

                // Hide after 3 seconds of no scrolling
                LaunchedEffect(listState.isScrollInProgress) {
                    if (!listState.isScrollInProgress && showScrollButton) {
                        kotlinx.coroutines.delay(3000)
                        if (!listState.isScrollInProgress) {
                            showScrollButton = false
                        }
                    }
                }

                // Hide when busy or can't scroll
                LaunchedEffect(isNotBusy, canScroll) {
                    if (!isNotBusy || !canScroll) {
                        showScrollButton = false
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)
                ) {
                    // Scroll up/down button (separate from share)
                    AnimatedVisibility(
                        visible = showScrollButton,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = scaleOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        ScrollFAB(
                            modifier = Modifier,
                            listState = listState,
                            scrollDirection = lastScrollDirection
                        )
                    }

                    // Share button (always visible when output exists, like save button)
                    ShareFAB()

                    BottomExtendedFAB(
                        listState = listState,
                        onClickSave = { success ->
                            handleSaveButtonClick(success)
                        })
                }
            }
        })
    { innerPadding ->
        SharedTransitionLayout {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = isOutputFullscreen,
                    label = "fullscreen_transition",
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                                    ))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
                        } else {
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
                                .togetherWith(
                                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                            scaleOut(
                                                targetScale = 0.9f,
                                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                                            )
                                )
                        }
                    }
                ) { fullscreen ->
                    if (fullscreen) {
                        // Fullscreen output overlay - covers all UI and ignores innerPadding
                        FullscreenOutputOverlay(
                            onDismiss = { scrollIndex ->
                                restoredScrollIndex = scrollIndex
                                isOutputFullscreen = false
                            },
                            initialScrollIndex = listState.firstVisibleItemIndex,
                            shellViewModel = shellViewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            UtilityButtonGroup(
                                showClearOutputDialog = { dialogManager.show(DialogKey.Shell.ClearOutput) },
                                handleClearOutput = { handleClearOutput() },
                                showBookmarkDialog = {
                                    handleBookmarkButtonClick()
                                },
                                showHistoryMenu = { handleHistoryButtonClick() })

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(25.dp)
                            ) {
                                AutoResizeableText(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                // Extra button content (e.g., file browser)
                                extraButtonContent?.invoke()

                                TextButton(
                                    onClick = withHaptic {
                                        modeButtonOnClick()
                                    },
                                    shapes = ButtonDefaults.shapes(),
                                    modifier = Modifier.animateContentSize(),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    AutoResizeableText(
                                        text = modeButtonText,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 5.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                val label =
                                    if (states.commandField.isError) states.commandField.errorMessage else stringResource(
                                        R.string.command_title
                                    )

                                val isBookmarked =
                                    bookmarkViewModel.isBookmarked(states.commandField.fieldValue.text)
                                        .collectAsState(initial = false)
                                val trailingIcon =
                                    if (isBookmarked.value) painterResource(R.drawable.ic_bookmark_added) else painterResource(
                                        R.drawable.ic_add_bookmark
                                    )
                                val overrideBookmarksLimit =
                                    LocalSettings.current.overrideBookmarksLimit

                                ExposedDropdownMenuBox(
                                    modifier = Modifier.weight(1f),
                                    expanded = historyMenuExpanded,
                                    onExpandedChange = {}) {

                                    OutlinedTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .focusRequester(textFieldFocusRequester)
                                            .menuAnchor(
                                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                                enabled = true
                                            ),
                                        maxLines = 3,
                                        label = { Text(label) },
                                        value = states.commandField.fieldValue,
                                        onValueChange = { shellViewModel.onCommandTextFieldChange(it) },
                                        isError = states.commandField.isError,
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = { actionFabOnClick() }
                                        ),
                                        trailingIcon = {
                                            if (states.commandField.fieldValue.text.trim()
                                                    .isNotEmpty()
                                            )
                                                IconButton(
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = Color.Transparent,
                                                        contentColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    onClick = withHaptic {
                                                        if (isBookmarked.value) bookmarkViewModel.deleteBookmark(
                                                            states.commandField.fieldValue.text
                                                        )
                                                        else if (bookmarkCount.value >= 25 && !overrideBookmarksLimit) {
                                                            hideKeyboard(context)
                                                            coroutineScope.launch {
                                                                snackBarHostState.showSnackbar(
                                                                    message = context.getString(R.string.bookmark_limit_reached),
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        } else bookmarkViewModel.addBookmark(states.commandField.fieldValue.text)
                                                    }) {
                                                    Icon(
                                                        painter = trailingIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                    )
                                                }
                                        })

                                    ExposedDropdownMenu(
                                        expanded = historyMenuExpanded,
                                        onDismissRequest = { historyMenuExpanded = false },
                                        modifier = Modifier
                                            .heightIn(max = 400.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        if (states.cmdHistory.isEmpty()) return@ExposedDropdownMenu

                                        states.cmdHistory.reversed().forEach { command ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = command,
                                                        maxLines = 1,
                                                        modifier = Modifier.basicMarquee()
                                                    )
                                                },
                                                onClick = {
                                                    shellViewModel.onCommandTextFieldChange(
                                                        TextFieldValue(
                                                            command
                                                        )
                                                    )
                                                    shellViewModel.updateTextFieldSelection()
                                                    historyMenuExpanded = false
                                                    textFieldFocusRequester.requestFocus()
                                                }
                                            )
                                        }
                                    }
                                }

                                FloatingActionButton(
                                    modifier = Modifier.padding(top = 10.dp),
                                    onClick = actionFabOnClick,
                                    containerColor = if (states.shellState is ShellState.Busy) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                    contentColor = if (states.shellState is ShellState.Busy) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    content = actionFabIcon
                                )
                            }

                            if (states.commandField.fieldValue.text.isNotEmpty() && !states.search.isVisible) {
                                Suggestions(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutputCard(
                                listState = listState,
                                isFullscreen = isOutputFullscreen,
                                onFullscreenToggle = { isOutputFullscreen = !isOutputFullscreen },
                                restoredScrollIndex = restoredScrollIndex,
                                onScrollRestored = { restoredScrollIndex = -1 },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent
                            )
                        }

                        if (states.search.textFieldValue.text.isNotEmpty() && searchOutputResult.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                NoSearchResultUi(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 40.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (dialogManager.activeDialog) {
        DialogKey.Shell.ClearOutput -> ClearOutputConfirmationDialog(
            onDismiss = { dialogManager.dismiss() },
            onConfirm = { handleClearOutput() })

        DialogKey.Shell.Bookmark -> BookmarkDialog(
            onBookmarkClicked = { command ->
                shellViewModel.onCommandTextFieldChange(TextFieldValue(command))
                shellViewModel.updateTextFieldSelection()
                dialogManager.dismiss()
                textFieldFocusRequester.requestFocus()
            },
            onDelete = { dialogManager.show(DialogKey.Shell.DeleteBookmarks) },
            onSort = { dialogManager.show(DialogKey.Shell.BookmarkSort) },
            onDismiss = { dialogManager.dismiss() }
        )

        DialogKey.Shell.DeleteBookmarks -> DeleteBookmarksDialog(
            onDismiss = { dialogManager.show(DialogKey.Shell.Bookmark) },
            onDelete = {
                dialogManager.dismiss()
                bookmarkViewModel.deleteAllBookmark()
            }
        )

        DialogKey.Shell.BookmarkSort -> BookmarksSortDialog(
            onDismiss = { dialogManager.show(DialogKey.Shell.Bookmark) }
        )

        DialogKey.Shell.FileSaved -> FileSavedDialog(
            onDismiss = { dialogManager.dismiss() },
            onOpenFile = { handleSavedFileOpen() },
        )

        else -> dialogManager.dismiss()
    }

    extraContent()
}

@Composable
fun Suggestions(
    modifier: Modifier = Modifier,
    viewModel: ShellViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val suggestions by viewModel.suggestions.collectAsState()

    LaunchedEffect(suggestions) {
        if (suggestions.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            count = suggestions.size,
            key = { index -> suggestions[index].id }) { index ->
            val shape = getRoundedShape(index = index, size = suggestions.size)

            SuggestionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                suggestion = suggestions[index],
                roundedCornerShape = shape
            )
        }
    }
}

@Composable
private fun NoSearchResultUi(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Image(
            imageVector = DynamicColorImageVectors.noSearchResult(),
            contentDescription = null,
        )

        AutoResizeableText(
            text = stringResource(R.string.no_search_results_found),
            style = MaterialTheme.typography.bodyMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
    }
}

@Composable
private fun OutputCard(
    listState: LazyListState,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    restoredScrollIndex: Int = -1,
    onScrollRestored: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val isDarkMode = LocalDarkMode.current
    val terminalFontStyle = LocalSettings.current.terminalFontStyle

    val commandTextStyle =
        if (terminalFontStyle == TerminalFontStyle.MONOSPACE) MaterialTheme.typography.titleSmallEmphasized.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        else MaterialTheme.typography.titleSmallEmphasized.copy(
            fontWeight = FontWeight.SemiBold
        )

    val bodyTextStyle =
        if (terminalFontStyle == TerminalFontStyle.MONOSPACE) MaterialTheme.typography.bodySmallEmphasized.copy(
            fontFamily = FontFamily.Monospace
        ) else MaterialTheme.typography.bodySmallEmphasized

    val states by shellViewModel.states.collectAsState()
    val results by shellViewModel.filteredOutput.collectAsState()

    val allOutputs = results.map { commandResult ->
        commandResult.outputFlow.collectAsState()
    }

    val combinedOutput = remember(results, allOutputs) {
        derivedStateOf {
            if (results.isEmpty() || allOutputs.isEmpty()) {
                emptyList()
            } else {
                allOutputs.flatMapIndexed { index, outputState ->
                    val command =
                        results.getOrNull(index)?.command ?: return@flatMapIndexed emptyList()
                    listOf(OutputLine("$ $command", isError = false)) + outputState.value
                }
            }
        }
    }

    if (combinedOutput.value.isEmpty()) return

    // Restore scroll position when exiting fullscreen
    LaunchedEffect(restoredScrollIndex) {
        if (restoredScrollIndex >= 0 && combinedOutput.value.isNotEmpty()) {
            val safeIndex = restoredScrollIndex.coerceAtMost(combinedOutput.value.lastIndex)
            listState.scrollToItem(safeIndex)
            onScrollRestored()
        }
    }

    // Smart auto-scroll during live output (ShellState.Busy)
    var userScrolledAway by remember { mutableStateOf(false) }
    var autoScrollResumeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val outputCardScope = rememberCoroutineScope()

    // Check if we're near the bottom (within 3 items)
    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems == 0 || lastVisibleIndex >= totalItems - 3
        }
    }

    // Detect user scroll using scroll position changes
    var lastScrollIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.isScrollInProgress,
                listState.layoutInfo.totalItemsCount
            )
        }.collect { (currentIndex, isScrolling, totalItems) ->
            if (isScrolling && totalItems > 0) {
                // User scrolled UP (away from bottom) - pause auto-scroll
                if (currentIndex < lastScrollIndex && !isNearBottom) {
                    userScrolledAway = true
                    // Cancel any existing resume job and start new 3s timer
                    autoScrollResumeJob?.cancel()
                    autoScrollResumeJob = outputCardScope.launch {
                        kotlinx.coroutines.delay(3000)
                        userScrolledAway = false
                    }
                }
                // User scrolled DOWN to bottom - resume auto-scroll immediately
                else if (isNearBottom && userScrolledAway) {
                    autoScrollResumeJob?.cancel()
                    userScrolledAway = false
                }
            }
            lastScrollIndex = currentIndex
        }
    }

    // Track previous busy state to detect command completion
    var wasBusy by remember { mutableStateOf(false) }

    // Reset scroll state and scroll to bottom when command finishes
    LaunchedEffect(states.shellState, combinedOutput.value.size) {
        val isBusy = states.shellState is ShellState.Busy
        
        // When command finishes (was busy, now not)
        if (wasBusy && !isBusy && combinedOutput.value.isNotEmpty() && !isFullscreen) {
            userScrolledAway = false
            autoScrollResumeJob?.cancel()
            try {
                // Delay slightly to let UI settle, then scroll to bottom
                kotlinx.coroutines.delay(50)
                listState.animateScrollToItem(combinedOutput.value.lastIndex)
            } catch (_: Exception) {
                // Ignore scroll cancellation
            }
        }
        
        wasBusy = isBusy
    }

    // Auto-scroll to bottom during live output
    LaunchedEffect(combinedOutput.value.size, states.shellState, userScrolledAway) {
        if (states.shellState is ShellState.Busy && !userScrolledAway && combinedOutput.value.isNotEmpty() && !isFullscreen) {
            try {
                listState.scrollToItem(combinedOutput.value.lastIndex)
            } catch (_: Exception) {
                // Ignore scroll cancellation
            }
        }
    }

    val cardContainerColor = if (isDarkMode)
        MaterialTheme.colorScheme.surfaceContainerLowest
    else MaterialTheme.colorScheme.surfaceVariant

    val headerColor = if (isDarkMode)
        MaterialTheme.colorScheme.surfaceContainerLow
    else MaterialTheme.colorScheme.surfaceContainer

    // Only render the card when not in fullscreen
    if (!isFullscreen) {
        with(sharedTransitionScope) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "output_card"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .clip(MaterialTheme.shapes.large),
                colors = CardDefaults.cardColors(
                    containerColor = cardContainerColor,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // Header bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerColor)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.output),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = withHaptic(HapticFeedbackType.VirtualKey) { onFullscreenToggle() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Output content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(combinedOutput.value) { _, line ->
                                OutputLineItem(
                                    line = line,
                                    states = states,
                                    commandTextStyle = commandTextStyle,
                                    bodyTextStyle = bodyTextStyle
                                )
                            }
                        }

                        VerticalScrollbar(
                            listState = listState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputLineItem(
    line: OutputLine,
    states: ShellScreenState,
    commandTextStyle: androidx.compose.ui.text.TextStyle,
    bodyTextStyle: androidx.compose.ui.text.TextStyle
) {
    val text = if (!states.search.isVisible) line.text else line.text.takeIf {
        line.text.contains(
            states.search.textFieldValue.text,
            ignoreCase = true
        )
    }

    val isCommandLine = text?.startsWith("$ ")

    val lineColor =
        if (isCommandLine == true) MaterialTheme.colorScheme.primary else {
            if (line.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        }

    val textStyle = if (isCommandLine == true) commandTextStyle else bodyTextStyle

    text?.let {
        val annotatedText =
            if (states.search.isVisible && !states.search.textFieldValue.text.isBlank()) {
                val highlightBgColor =
                    if (line.isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                val highlightTextColor =
                    if (line.isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

                highlightQueryText(
                    text = text,
                    query = states.search.textFieldValue.text,
                    highlightBgColor = highlightBgColor,
                    highlightTextColor = highlightTextColor
                )
            } else {
                AnnotatedString(text)
            }

        Text(
            text = annotatedText,
            style = textStyle,
            color = lineColor,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCommandLine == true) Modifier.padding(
                        top = 20.dp,
                        bottom = 10.dp
                    ) else Modifier
                )
        )
    }
}

@Composable
private fun FullscreenOutputOverlay(
    onDismiss: (scrollIndex: Int) -> Unit,
    initialScrollIndex: Int = 0,
    shellViewModel: ShellViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val terminalFontStyle = LocalSettings.current.terminalFontStyle
    val fullscreenListState =
        rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)

    val commandTextStyle =
        if (terminalFontStyle == TerminalFontStyle.MONOSPACE) MaterialTheme.typography.titleSmallEmphasized.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        else MaterialTheme.typography.titleSmallEmphasized.copy(
            fontWeight = FontWeight.SemiBold
        )

    val bodyTextStyle =
        if (terminalFontStyle == TerminalFontStyle.MONOSPACE) MaterialTheme.typography.bodySmallEmphasized.copy(
            fontFamily = FontFamily.Monospace
        ) else MaterialTheme.typography.bodySmallEmphasized

    val states by shellViewModel.states.collectAsState()
    val results by shellViewModel.filteredOutput.collectAsState()

    val allOutputs = results.map { commandResult ->
        commandResult.outputFlow.collectAsState()
    }

    val combinedOutput = remember(results, allOutputs) {
        derivedStateOf {
            if (results.isEmpty() || allOutputs.isEmpty()) {
                emptyList()
            } else {
                allOutputs.flatMapIndexed { index, outputState ->
                    val command =
                        results.getOrNull(index)?.command ?: return@flatMapIndexed emptyList()
                    listOf(OutputLine("$ $command", isError = false)) + outputState.value
                }
            }
        }
    }

    BackHandler(enabled = true) {
        // Cache scroll index to avoid reading it during composition
        onDismiss(fullscreenListState.firstVisibleItemIndex)
    }

    // Track if user has scrolled away from bottom
    var userScrolledAway by remember { mutableStateOf(false) }
    var lastScrollPosition by remember { mutableIntStateOf(0) }
    var autoScrollJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Check if we're near the bottom (within 3 items)
    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = fullscreenListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems == 0 || lastVisibleIndex >= totalItems - 3
        }
    }

    // Detect user scroll using scroll position changes
    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(
                fullscreenListState.firstVisibleItemIndex,
                fullscreenListState.isScrollInProgress,
                fullscreenListState.layoutInfo.totalItemsCount
            )
        }.collect { (currentIndex, isScrolling, totalItems) ->
            if (isScrolling && totalItems > 0) {
                // User scrolled UP (away from bottom) - pause auto-scroll
                if (currentIndex < lastScrollPosition && !isNearBottom) {
                    userScrolledAway = true
                    // Cancel any existing resume job and start new 3s timer
                    autoScrollJob?.cancel()
                    autoScrollJob = coroutineScope.launch {
                        kotlinx.coroutines.delay(3000)
                        userScrolledAway = false
                    }
                }
            }
            lastScrollPosition = currentIndex
        }
    }

    // Auto-scroll to absolute bottom during live output
    LaunchedEffect(combinedOutput.value.size, states.shellState) {
        if (states.shellState is ShellState.Busy && !userScrolledAway && combinedOutput.value.isNotEmpty()) {
            try {
                // Use scrollToItem for instant positioning (no animation during rapid output)
                fullscreenListState.scrollToItem(combinedOutput.value.lastIndex)
            } catch (_: Exception) {
                // Ignore scroll cancellation
            }
        }
    }

    // When command finishes, always scroll to absolute bottom
    LaunchedEffect(states.shellState) {
        if (states.shellState !is ShellState.Busy && combinedOutput.value.isNotEmpty()) {
            userScrolledAway = false
            try {
                fullscreenListState.animateScrollToItem(combinedOutput.value.lastIndex)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    val containerColor = MaterialTheme.colorScheme.surface

    with(sharedTransitionScope) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .sharedElement(
                    rememberSharedContentState(key = "output_card"),
                    animatedVisibilityScope = animatedContentScope
                ),
            color = containerColor
        ) {
            Scaffold(
                containerColor = containerColor,
                topBar = {
                    val coroutineScope = rememberCoroutineScope()
                    val smoothScroll = LocalSettings.current.smoothScrolling

                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.output),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                onDismiss(
                                    fullscreenListState.firstVisibleItemIndex
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.FullscreenExit,
                                    contentDescription = "Exit fullscreen"
                                )
                            }
                        },
                        actions = {
                            if (states.shellState !is ShellState.Busy) {
                                // Scroll to top
                                IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    coroutineScope.launch {
                                        if (smoothScroll) fullscreenListState.animateScrollToItem(0)
                                        else fullscreenListState.scrollToItem(0)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardDoubleArrowUp,
                                        contentDescription = "Scroll to top"
                                    )
                                }
                                // Scroll to bottom
                                IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    coroutineScope.launch {
                                        val lastIndex =
                                            fullscreenListState.layoutInfo.totalItemsCount - 1
                                        if (lastIndex >= 0) {
                                            if (smoothScroll) fullscreenListState.animateScrollToItem(
                                                lastIndex
                                            )
                                            else fullscreenListState.scrollToItem(lastIndex)
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardDoubleArrowDown,
                                        contentDescription = "Scroll to bottom"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { paddingValues ->

                // Group output lines by command sections
                val commandSections = remember(combinedOutput.value) {
                    val sections = mutableListOf<Pair<String, List<OutputLine>>>()
                    var currentCommand: String? = null
                    var currentLines = mutableListOf<OutputLine>()

                    combinedOutput.value.forEach { line ->
                        if (line.text.startsWith("$ ")) {
                            // Save previous section if exists
                            if (currentCommand != null && currentLines.isNotEmpty()) {
                                sections.add(currentCommand to currentLines.toList())
                            }
                            currentCommand = line.text
                            currentLines = mutableListOf()
                        } else if (currentCommand != null) {
                            currentLines.add(line)
                        }
                    }
                    // Add last section
                    if (currentCommand != null) {
                        sections.add(currentCommand to currentLines.toList())
                    }
                    sections
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = fullscreenListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commandSections.forEachIndexed { sectionIndex, (command, lines) ->
                            stickyHeader(key = "header_$sectionIndex") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                            .padding(horizontal = 20.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = command,
                                            style = commandTextStyle,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 1.dp
                                    )
                                }
                            }

                            itemsIndexed(
                                items = lines,
                                key = { index, _ -> "${sectionIndex}_$index" }
                            ) { _, line ->
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    OutputLineItem(
                                        line = line,
                                        states = states,
                                        commandTextStyle = commandTextStyle,
                                        bodyTextStyle = bodyTextStyle
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }

                    VerticalScrollbar(
                        listState = fullscreenListState,
                        thumbSize = 20,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(8.dp)
                            .padding(end = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollFAB(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    scrollDirection: ScrollDirection,
) {
    val coroutineScope = rememberCoroutineScope()
    val smoothScroll = LocalSettings.current.smoothScrolling

    val icon = when (scrollDirection) {
        ScrollDirection.UP -> Icons.Rounded.KeyboardDoubleArrowUp
        ScrollDirection.DOWN -> Icons.Rounded.KeyboardDoubleArrowDown
        else -> null
    }

    icon?.let {
        SmallFloatingActionButton(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = {
                coroutineScope.launch {
                    val targetIndex = when (scrollDirection) {
                        ScrollDirection.UP -> 0
                        ScrollDirection.DOWN -> listState.layoutInfo.totalItemsCount - 1
                        else -> return@launch
                    }

                    if (targetIndex < 0) return@launch

                    if (smoothScroll) listState.animateScrollToItem(targetIndex)
                    else listState.scrollToItem(targetIndex)
                }

            }
        ) {
            Icon(
                imageVector = it,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ShareFAB(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val states by shellViewModel.states.collectAsState()

    if (states.output.isEmpty()) return

    val icon = Icons.Rounded.Share

    val shareAction: () -> Unit = {
        val outputText = buildString {
            states.output.forEachIndexed { index, commandResult ->
                appendLine("$ ${commandResult.command}")
                val lines = commandResult.outputFlow.value
                lines.forEach { line ->
                    appendLine(line.text)
                }
                appendLine()
            }
        }

        activity?.let { act ->
            try {
                val fileName = shellViewModel.getSaveOutputFileName(true)
                val tempFile = File(act.cacheDir, fileName)
                tempFile.writeText(outputText)

                val uri = FileProvider.getUriForFile(
                    act,
                    "${act.packageName}.fileprovider",
                    tempFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                act.startActivity(Intent.createChooser(shareIntent, "Share command output"))

            } catch (e: Exception) {
                e.printStackTrace()
                showToast(context, context.getString(R.string.failed))
            }
        }
    }

    SmallFloatingActionButton(
        onClick = withHaptic {
            shareAction()
        }, containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
private fun BottomExtendedFAB(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    onClickSave: (success: Boolean) -> Unit,
    shellViewModel: ShellViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()
    val states by shellViewModel.states.collectAsState()
    val savePath = LocalSettings.current.outputSaveDirectory.toUri()
    val saveWholeOutput = LocalSettings.current.saveWholeOutput

    var lastScrollOffset by remember { mutableIntStateOf(0) }

    val expanded by remember {
        derivedStateOf {
            listState.shouldFabExpand(states.output, lastScrollOffset) {
                lastScrollOffset = it
            }
        }
    }

    val icon =
        if (states.output.isEmpty()) painterResource(R.drawable.ic_paste) else painterResource(R.drawable.ic_save)

    val buttonText =
        if (states.output.isEmpty()) stringResource(R.string.paste) else stringResource(R.string.save)

    val saveAction: () -> Unit = {
        val allOutputText = buildString {
            states.output.forEachIndexed { index, commandResult ->
                appendLine("$ ${commandResult.command}")
                val lines = commandResult.outputFlow.value
                lines.forEach { line ->
                    appendLine(line.text)
                }
                appendLine()
            }
        }

        val lastOutputText = shellViewModel.getLastCommandOutput(allOutputText)

        val fileName = shellViewModel.getSaveOutputFileName(saveWholeOutput)

        val textToSave = if (saveWholeOutput) allOutputText else lastOutputText

        activity?.let {
            coroutineScope.launch {
                saveToFileFlow(
                    sb = textToSave,
                    activity = it,
                    fileName = fileName,
                    savePathUri = savePath
                ).collect { result ->
                    onClickSave(result.success)
                    if (result.success) settingsViewModel.setString(
                        key = SettingsKeys.LAST_SAVED_FILE_URI,
                        value = result.uri.toString()
                    )
                }
            }
        }
    }


    val pasteAction: () -> Unit = {
        val textInClipboard = ClipboardUtils.readFromClipboard(context) ?: ""
        if (textInClipboard.trim().isEmpty()) {
            showToast(context, context.getString(R.string.clipboard_empty))
        } else {
            shellViewModel.onCommandTextFieldChange(TextFieldValue(textInClipboard))
            shellViewModel.updateTextFieldSelection()
        }
    }

    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = withHaptic {
            if (states.output.isEmpty()) pasteAction() else saveAction()
        },
        expanded = expanded,
        icon = {
            Icon(
                painter = icon,
                contentDescription = null
            )
        },
        text = {
            AutoResizeableText(text = buttonText)
        }
    )
}

private fun LazyListState.shouldFabExpand(
    results: List<CommandResult>,
    lastScrollOffset: Int,
    onScrollOffsetChanged: (Int) -> Unit
): Boolean {
    val currentOffset = firstVisibleItemIndex * 1000 + firstVisibleItemScrollOffset
    val isScrollingUp = currentOffset < lastScrollOffset
    val isScrollingDown = currentOffset > lastScrollOffset
    onScrollOffsetChanged(currentOffset)

    val info = layoutInfo
    val visible = info.visibleItemsInfo
    val lastVisible = visible.lastOrNull()

    val fitsOnScreen = if (info.totalItemsCount == 0 || lastVisible == null) {
        true
    } else {
        val allItemsVisible = info.totalItemsCount == visible.size
        val lastBottom = lastVisible.offset + lastVisible.size
        allItemsVisible && lastBottom <= info.viewportEndOffset
    }

    return when {
        results.isEmpty() -> true
        fitsOnScreen -> true
        isScrollingUp -> true
        isScrollingDown -> false
        else -> false
    }
}