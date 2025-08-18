@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.presentation.screens

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.presentation.ui.utils.disableKeyboard
import `in`.hridayan.ashell.core.presentation.ui.utils.hideKeyboard
import `in`.hridayan.ashell.core.presentation.ui.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.presentation.viewmodel.BookmarkViewModel
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.CommandExamplesScreen
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.SettingsScreen
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarkDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.BookmarksSortDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.ClearOutputConfirmationDialog
import `in`.hridayan.ashell.shell.presentation.components.dialog.DeleteBookmarksDialog
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

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
    val command by shellViewModel.command.collectAsState()
    val commandError by shellViewModel.commandError.collectAsState()
    val shellState by shellViewModel.shellState.collectAsState()
    val isKeyboardVisible = isKeyboardVisible().value
    val disableSoftKeyboard = LocalSettings.current.disableSoftKeyboard
    val bookmarkCount = bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)
    val textFieldFocusRequester = remember { FocusRequester() }
    val history = rememberSaveable { mutableStateListOf<String>() }
    var historyMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var showClearOutputDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showBookmarkSortDialog by rememberSaveable { mutableStateOf(false) }

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
                    history.add(command.text)
                }
            }

            is ShellState.Busy -> {
                shellViewModel.stopCommand()
            }

            is ShellState.Free -> {
                navController.navigate(CommandExamplesScreen)
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
        if (history.isEmpty()) showToast(
            context,
            context.getString(R.string.no_history)
        ) else historyMenuExpanded = true
    }

    val handleSearchButtonClick: () -> Unit = {
        if (commandResults.isEmpty()) showToast(
            context,
            context.getString(R.string.nothing_to_search)
        ) else showSearchBar = true
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) })
    { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                UtilityButtonsRow(
                    showClearOutputDialog = { showClearOutputDialog = true },
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
                            if (history.isEmpty()) return@ExposedDropdownMenu

                            history.reversed().forEach { command ->
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
                        modifier = Modifier.padding(top = 6.dp),
                        content = actionFabIcon
                    )
                }

                OutputCard(results = commandResults)
            }
        }

        if (showClearOutputDialog) {
            ClearOutputConfirmationDialog(
                onDismiss = { showClearOutputDialog = false },
                onConfirm = { shellViewModel.clearOutput() })
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

        extraContent()
    }
}

@Composable
private fun OutputCard(results: List<CommandResult>) {
    val listState = rememberLazyListState()
    val isDarkMode = LocalDarkMode.current

    val allOutputs = results.map { commandResult ->
        commandResult.outputFlow.collectAsState()
    }

    val combinedOutput = remember(allOutputs) {
        derivedStateOf {
            allOutputs.flatMapIndexed { index, outputState ->
                val command = results[index].command
                listOf(OutputLine("$ $command", isError = false)) + outputState.value
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
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(combinedOutput.value) { _, line ->
                    val text = line.text

                    val isCommandLine = text.startsWith("$ ")

                    val lineColor =
                        if (isCommandLine) MaterialTheme.colorScheme.primary else {
                            if (line.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        }

                    val textStyle =
                        if (isCommandLine) MaterialTheme.typography.titleSmallEmphasized.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        else MaterialTheme.typography.bodySmallEmphasized.copy(fontFamily = FontFamily.Monospace)

                    Text(
                        text = text,
                        style = textStyle,
                        color = lineColor,
                        modifier = Modifier.then(
                            if (isCommandLine) Modifier.padding(
                                top = 20.dp,
                                bottom = 10.dp
                            ) else Modifier
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
@Composable
private fun AnimatedStopIcon(modifier: Modifier = Modifier) {
    val tintColor = MaterialTheme.colorScheme.onPrimaryContainer

    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                val avd =
                    context.getDrawable(R.drawable.ic_stop_animated) as? AnimatedVectorDrawable
                setImageDrawable(avd)
                setColorFilter(tintColor.toArgb(), PorterDuff.Mode.SRC_IN)
                avd?.start()
            }
        },
        modifier = modifier.size(24.dp)
    )
}

@Composable
fun UtilityButtonsRow(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel(),
    showClearOutputDialog: () -> Unit = {},
    showBookmarkDialog: () -> Unit = {},
    showHistoryMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val interactionSources = remember { List(5) { MutableInteractionSource() } }
    val askToClean = LocalSettings.current.clearOutputConfirmation
    val shellState by shellViewModel.shellState.collectAsState()

    @Suppress("DEPRECATION")
    ButtonGroup(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 25.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                weakHaptic()
            },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(40.dp)
                .animateWidth(interactionSources[0]),
            interactionSource = interactionSources[0],
        ) {
            TooltipContent(text = stringResource(R.string.search)) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = {
                weakHaptic()
                showBookmarkDialog()
            },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(40.dp)
                .animateWidth(interactionSources[1]),
            interactionSource = interactionSources[1],
        ) {
            TooltipContent(text = stringResource(R.string.bookmarks)) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmarks),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = {
                weakHaptic()
                showHistoryMenu()
            },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(40.dp)
                .animateWidth(interactionSources[2]),
            interactionSource = interactionSources[2],
        ) {
            TooltipContent(text = stringResource(R.string.history)) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = {
                weakHaptic()

                if (shellViewModel.commandResults.value.isEmpty()) {
                    showToast(context, context.getString(R.string.nothing_to_clear))
                    return@IconButton
                }

                if (shellState == ShellState.Busy) {
                    showToast(context, context.getString(R.string.abort_command))
                    return@IconButton
                }

                if (askToClean) showClearOutputDialog()
                else shellViewModel.clearOutput()
            },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(40.dp)
                .animateWidth(interactionSources[3]),
            interactionSource = interactionSources[3],
        ) {
            TooltipContent(text = stringResource(R.string.clear)) {
                Icon(
                    painter = painterResource(R.drawable.ic_clear),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(
            onClick = {
                weakHaptic()
                navController.navigate(SettingsScreen)
            },
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(40.dp)
                .animateWidth(interactionSources[4]),
            interactionSource = interactionSources[4],
        ) {
            TooltipContent(text = stringResource(R.string.settings)) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
