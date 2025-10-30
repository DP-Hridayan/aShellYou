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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.ScrollDirection
import `in`.hridayan.ashell.core.presentation.components.scrollbar.VerticalScrollbar
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.utils.disableKeyboard
import `in`.hridayan.ashell.core.presentation.utils.hideKeyboard
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.presentation.viewmodel.BookmarkViewModel
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.findActivity
import `in`.hridayan.ashell.core.utils.saveToFileFlow
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.presentation.components.button.UtilityButtonGroup
import `in`.hridayan.ashell.shell.presentation.components.card.CommandSuggestionsCard
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarkDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarksSortDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.ClearOutputConfirmationDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.DeleteBookmarksDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.FileSavedDialog
import `in`.hridayan.ashell.shell.presentation.components.icon.AnimatedStopIcon
import `in`.hridayan.ashell.shell.presentation.model.CommandResult
import `in`.hridayan.ashell.shell.presentation.model.ShellState
import `in`.hridayan.ashell.shell.presentation.util.highlightQueryText
import `in`.hridayan.ashell.shell.presentation.util.rememberScrollDirection
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
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
    extraContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
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
    var showClearOutputDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkSortDialog by rememberSaveable { mutableStateOf(false) }
    var showFileSavedDialog by rememberSaveable { mutableStateOf(false) }

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

    val actionFabOnClick: () -> Unit = {
        weakHaptic()
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
        ) else showBookmarkDialog = true
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
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                UtilityButtonGroup(
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
                    val overrideBookmarksLimit = LocalSettings.current.overrideBookmarksLimit

                    ExposedDropdownMenuBox(
                        modifier = Modifier.weight(1f),
                        expanded = historyMenuExpanded,
                        onExpandedChange = {}) {

                        OutlinedTextField(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .focusRequester(textFieldFocusRequester),
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
                                if (states.commandField.fieldValue.text.trim().isNotEmpty())
                                    IconButton(
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        onClick = {
                                            weakHaptic()
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

                if (states.commandField.fieldValue.text.isNotEmpty() && !states.search.isVisible) {
                    CommandSuggestions(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutputCard(listState = listState)
            }

            if (states.search.textFieldValue.text.isNotEmpty() && searchOutputResult.isEmpty()) {
                NoSearchResultUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .align(Alignment.Center)
                )
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
                    shellViewModel.onCommandTextFieldChange(TextFieldValue(command))
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
fun CommandSuggestions(
    modifier: Modifier = Modifier,
    viewModel: ShellViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val commandSuggestions by viewModel.commandSuggestions.collectAsState()

    LaunchedEffect(commandSuggestions) {
        if (commandSuggestions.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            count = commandSuggestions.size,
            key = { index -> commandSuggestions[index].id }) { index ->
            val shape = getRoundedShape(index = index, size = commandSuggestions.size)

            CommandSuggestionsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                command = commandSuggestions[index].command,
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
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val isDarkMode = LocalDarkMode.current
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

                    val textStyle =
                        if (isCommandLine == true) MaterialTheme.typography.titleSmallEmphasized.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        else MaterialTheme.typography.bodySmallEmphasized.copy(
                            fontFamily = FontFamily.Monospace
                        )

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
        }
    }

    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = {
            weakHaptic()
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