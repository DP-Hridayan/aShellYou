@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)

package `in`.hridayan.ashell.shell.presentation.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.ScrollDirection
import `in`.hridayan.ashell.core.presentation.components.scrollbar.VerticalScrollbar
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.ui.utils.disableKeyboard
import `in`.hridayan.ashell.core.presentation.ui.utils.hideKeyboard
import `in`.hridayan.ashell.core.presentation.ui.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.presentation.viewmodel.BookmarkViewModel
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.findActivity
import `in`.hridayan.ashell.core.utils.saveToFileFlow
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.presentation.components.button.UtilityButtonGroup
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarkDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarksSortDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.ClearOutputConfirmationDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.DeleteBookmarksDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.FileSavedDialog
import `in`.hridayan.ashell.shell.presentation.components.icon.AnimatedStopIcon
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
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
    extraContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val commandResults by shellViewModel.commandResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scrollDirection = rememberScrollDirection(listState)
    val command by shellViewModel.command.collectAsState()
    val commandError by shellViewModel.commandError.collectAsState()
    val shellState by shellViewModel.shellState.collectAsState()
    val isKeyboardVisible = isKeyboardVisible().value
    val disableSoftKeyboard = LocalSettings.current.disableSoftKeyboard
    val bookmarkCount = bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)
    val lastSavedFileUri = LocalSettings.current.lastSavedFileUri
    val textFieldFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val history = shellViewModel.history.collectAsState(initial = emptyList())
    var historyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showClearOutputDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkSortDialog by rememberSaveable { mutableStateOf(false) }
    var showFileSavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(disableSoftKeyboard) {
        disableKeyboard(context, disableSoftKeyboard)
    }

    val actionFabIcon: @Composable () -> Unit = {
        when (shellState) {
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

    val actionFabOnClick: () -> Unit = {
        when (shellState) {
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
        ) else showBookmarkDialog = true
    }

    val handleHistoryButtonClick: () -> Unit = {
        if (history.value.isEmpty()) showToast(
            context,
            context.getString(R.string.no_history)
        ) else historyMenuExpanded = true
    }

    val handleClearOutput: () -> Unit = {
        shellViewModel.clearOutput()
        focusManager.clearFocus()
    }

    val handleSearchButtonClick: () -> Unit = {
        if (commandResults.isEmpty()) showToast(
            context,
            context.getString(R.string.nothing_to_search)
        )
    }

    val handleSaveButtonClick: (success: Boolean) -> Unit = { success ->
        showFileSavedDialog = success
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)
            ) {
                AnimatedContent(
                    targetState = scrollDirection != ScrollDirection.NONE,
                    transitionSpec = {
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ).togetherWith(
                            scaleOut(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        )
                    }
                ) { isScrolling ->
                    if (isScrolling) {
                        ScrollFAB(
                            modifier = Modifier,
                            listState = listState,
                            scrollDirection = scrollDirection
                        )
                    } else {
                        ShareFAB()
                    }
                }


                BottomExtendedFAB(
                    listState = listState,
                    onClickSave = { success ->
                        handleSaveButtonClick(success)
                    })
            }
        })
    { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                UtilityButtonGroup(
                    isOutputEmpty = commandResults.isEmpty(),
                    showClearOutputDialog = { showClearOutputDialog = true },
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

                    TextButton(
                        onClick = {
                            weakHaptic()
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
                        if (commandError) stringResource(R.string.field_cannot_be_blank) else stringResource(
                            R.string.command_title
                        )

                    val isBookmarked = bookmarkViewModel.isBookmarked(command.text)
                        .collectAsState(initial = false)
                    val trailingIcon =
                        if (isBookmarked.value) painterResource(R.drawable.ic_bookmark_added) else painterResource(
                            R.drawable.ic_add_bookmark
                        )
                    val overrideBookmarksLimit = LocalSettings.current.overrideBookmarksLimit

                    ExposedDropdownMenuBox(
                        modifier = Modifier.weight(1f),
                        expanded = historyMenuExpanded,
                        onExpandedChange = {}) {

                        OutlinedTextField(
                            modifier = Modifier
                                .focusRequester(textFieldFocusRequester),
                            label = { Text(label) },
                            value = command,
                            onValueChange = { shellViewModel.onCommandChange(it) },
                            trailingIcon = {
                                if (command.text.trim().isNotEmpty())
                                    IconButton(
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        onClick = {
                                            weakHaptic()
                                            if (isBookmarked.value) bookmarkViewModel.deleteBookmark(
                                                command.text
                                            )
                                            else if (bookmarkCount.value >= 25 && !overrideBookmarksLimit) {
                                                hideKeyboard(context)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = context.getString(R.string.bookmark_limit_reached),
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            } else bookmarkViewModel.addBookmark(command.text)
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
                            if (history.value.isEmpty()) return@ExposedDropdownMenu

                            history.value.reversed().forEach { command ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = command,
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee()
                                        )
                                    },
                                    onClick = {
                                        shellViewModel.onCommandChange(TextFieldValue(command))
                                        historyMenuExpanded = false
                                        textFieldFocusRequester.requestFocus()
                                    }
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = actionFabOnClick,
                        modifier = Modifier.padding(top = 10.dp),
                        content = actionFabIcon
                    )
                }

                OutputCard(listState = listState)
            }
        }

        if (showClearOutputDialog) {
            ClearOutputConfirmationDialog(
                onDismiss = { showClearOutputDialog = false },
                onConfirm = { handleClearOutput() })
        }

        if (showBookmarkDialog) {
            BookmarkDialog(
                onBookmarkClicked = { command ->
                    shellViewModel.onCommandChange(TextFieldValue(command))
                    showBookmarkDialog = false
                    textFieldFocusRequester.requestFocus()
                },
                onDelete = {
                    showDeleteConfirmationDialog = true
                    showBookmarkDialog = false
                },
                onSort = {
                    showBookmarkSortDialog = true
                    showBookmarkDialog = false
                },
                onDismiss = { showBookmarkDialog = false }
            )
        }

        if (showDeleteConfirmationDialog) {
            DeleteBookmarksDialog(
                onDismiss = {
                    showDeleteConfirmationDialog = false
                    showBookmarkDialog = true
                },
                onDelete = {
                    showBookmarkDialog = false
                    showDeleteConfirmationDialog = false
                    bookmarkViewModel.deleteAllBookmark()
                }
            )
        }

        if (showBookmarkSortDialog) {
            BookmarksSortDialog(
                onDismiss = {
                    showBookmarkDialog = true
                    showBookmarkSortDialog = false
                }
            )
        }

        if (showFileSavedDialog) {
            FileSavedDialog(
                onDismiss = {
                    showFileSavedDialog = false
                },
                onOpenFile = { handleSavedFileOpen() },
            )
        }

        extraContent()
    }
}

@Composable
private fun OutputCard(
    listState: LazyListState,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val isDarkMode = LocalDarkMode.current
    val isSearchVisible = shellViewModel.isSearchBarVisible.collectAsState()
    val searchQuery = shellViewModel.searchQuery.collectAsState()
    val results by shellViewModel.commandResults.collectAsState()

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

    LaunchedEffect(combinedOutput.value.size) {
        if (combinedOutput.value.isNotEmpty()) {
            listState.scrollToItem(combinedOutput.value.lastIndex)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode)
                MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
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
                itemsIndexed(combinedOutput.value) { index, line ->
                    val text = if (!isSearchVisible.value) line.text else line.text.takeIf {
                        line.text.contains(
                            searchQuery.value,
                            ignoreCase = true
                        )
                    }

                    val isCommandLine = text?.startsWith("$ ")

                    val lineColor =
                        if (isCommandLine == true) MaterialTheme.colorScheme.primary else {
                            if (line.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        }

                    val textStyle =
                        if (isCommandLine == true) MaterialTheme.typography.titleSmallEmphasized.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        else MaterialTheme.typography.bodySmallEmphasized.copy(
                            fontFamily = FontFamily.Monospace
                        )

                    text?.let {
                        Text(
                            text = text,
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
    val weakHaptic = LocalWeakHaptic.current
    val results by shellViewModel.commandResults.collectAsState()
    if (results.isEmpty()) return

    val icon = Icons.Rounded.Share

    val shareAction: () -> Unit = {
        val outputText = buildString {
            results.forEachIndexed { index, commandResult ->
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
        onClick = {
            weakHaptic()
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
    val weakHaptic = LocalWeakHaptic.current
    val coroutineScope = rememberCoroutineScope()
    val results by shellViewModel.commandResults.collectAsState()
    val savePath = LocalSettings.current.outputSaveDirectory.toUri()
    val saveWholeOutput = LocalSettings.current.saveWholeOutput

    var lastScrollOffset by remember { mutableIntStateOf(0) }

    val expanded by remember {
        derivedStateOf {
            listState.shouldFabExpand(results, lastScrollOffset) {
                lastScrollOffset = it
            }
        }
    }

    val icon =
        if (results.isEmpty()) painterResource(R.drawable.ic_paste) else painterResource(R.drawable.ic_save)

    val buttonText =
        if (results.isEmpty()) stringResource(R.string.paste) else stringResource(R.string.save)

    val saveAction: () -> Unit = {
        val allOutputText = buildString {
            results.forEachIndexed { index, commandResult ->
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
            shellViewModel.onCommandChange(TextFieldValue(textInClipboard))
        }
    }

    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = {
            weakHaptic()
            if (results.isEmpty()) pasteAction() else saveAction()
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

@Composable
fun rememberScrollDirection(
    listState: LazyListState,
    fastThreshold: Int = 150,
    idleDelayMillis: Long = 2000
): ScrollDirection {
    var lastOffset by remember { mutableIntStateOf(0) }
    var direction by remember { mutableStateOf(ScrollDirection.NONE) }
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val isScrollable = layoutInfo.totalItemsCount > 0 &&
                    layoutInfo.visibleItemsInfo.sumOf { it.size } < layoutInfo.viewportEndOffset

            val atBottom = layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastItem ->
                lastItem.index == layoutInfo.totalItemsCount - 1 &&
                        lastItem.offset + lastItem.size <= layoutInfo.viewportEndOffset
            } ?: false

            if (!isScrollable || atBottom) null
            else listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { value ->
            if (value == null) {
                direction = ScrollDirection.NONE
                job?.cancel()
                job = null
            } else {
                val (index, offset) = value
                val currentOffset = index * 1000 + offset
                val dy = currentOffset - lastOffset

                when {
                    dy < -fastThreshold -> {
                        job?.cancel()
                        direction = ScrollDirection.UP
                        job = null
                    }

                    dy > fastThreshold -> {
                        job?.cancel()
                        direction = ScrollDirection.DOWN
                        job = null
                    }

                    else -> {
                        job?.cancel()
                        job = launch {
                            delay(idleDelayMillis)
                            direction = ScrollDirection.NONE
                        }
                    }
                }

                lastOffset = currentOffset
            }
        }
    }

    return direction
}
