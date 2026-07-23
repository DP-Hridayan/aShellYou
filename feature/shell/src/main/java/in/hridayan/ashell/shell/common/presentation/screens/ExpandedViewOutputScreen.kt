package `in`.hridayan.ashell.shell.common.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.domain.model.OutputLine
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scrollbar.VerticalScrollbar
import `in`.hridayan.ashell.core.presentation.components.selectioncontainer.LazySelectionContainer
import `in`.hridayan.ashell.core.presentation.components.selectioncontainer.lazySelectionItem
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.shell.common.presentation.components.text.OutputLineText
import `in`.hridayan.ashell.shell.common.presentation.model.ShellState
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ExpandedViewOutputScreen(
    onDismiss: (scrollIndex: Int) -> Unit,
    initialScrollIndex: Int = 0,
    shellViewModel: ShellViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val res = LocalResources.current
    val context = LocalContext.current
    val terminalFontStyle = LocalSettings.current[SettingsKeys.TerminalFontStyle]
    val fullscreenListState =
        rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)

    val commandTextStyle =
        MaterialTheme.typography.titleSmallEmphasized.run {
            if (terminalFontStyle == TerminalFontStyle.MONOSPACE) this.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            else this.copy(fontWeight = FontWeight.SemiBold)
        }


    val bodyTextStyle =
        MaterialTheme.typography.bodySmallEmphasized.run {
            if (terminalFontStyle == TerminalFontStyle.MONOSPACE) this.copy(
                fontFamily = FontFamily.Monospace
            )
            else this
        }


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
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }
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
                        delay(3000.milliseconds)
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
                    val smoothScroll = LocalSettings.current[SettingsKeys.SmoothScrolling]

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
                    val sections = mutableListOf<CommandSection>()
                    var currentCommand: String? = null
                    var currentCommandIndex = -1
                    var currentLines = mutableListOf<Pair<Int, OutputLine>>()

                    combinedOutput.value.forEachIndexed { globalIndex, line ->
                        if (line.text.startsWith("$ ")) {
                            // Save previous section if exists
                            if (currentCommand != null) {
                                sections.add(
                                    CommandSection(
                                        currentCommandIndex,
                                        currentCommand,
                                        currentLines.toList()
                                    )
                                )
                            }
                            currentCommand = line.text
                            currentCommandIndex = globalIndex
                            currentLines = mutableListOf()
                        } else if (currentCommand != null) {
                            currentLines.add(globalIndex to line)
                        }
                    }
                    // Add last section
                    if (currentCommand != null) {
                        sections.add(
                            CommandSection(
                                currentCommandIndex,
                                currentCommand,
                                currentLines.toList()
                            )
                        )
                    }
                    sections
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazySelectionContainer(
                        modifier = Modifier.fillMaxWidth(),
                        listState = fullscreenListState,
                        items = combinedOutput.value,
                        itemToText = { it.text },
                        onCopy = { success ->
                            val toastMessage =
                                if (success) res.getString(R.string.copied_to_clipboard)
                                else res.getString(R.string.failed_to_copy)

                            showToast(context, toastMessage)
                        },
                    ) { selectionState ->

                        LazyColumn(
                            state = fullscreenListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = paddingValues
                        ) {
                            commandSections.forEachIndexed { sectionIndex, section ->
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
                                                modifier = Modifier.lazySelectionItem(
                                                    index = section.commandGlobalIndex,
                                                    text = section.commandText,
                                                    selectionState = selectionState,
                                                    style = commandTextStyle
                                                ),
                                                text = section.commandText,
                                                style = commandTextStyle,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 1.dp
                                        )
                                    }
                                }

                                itemsIndexed(
                                    items = section.lines,
                                    key = { _, pair -> "${sectionIndex}_${pair.first}" }
                                ) { _, pair ->
                                    val globalIndex = pair.first
                                    val line = pair.second

                                    val isCommandLine = line.text.startsWith("$ ")

                                    val textStyle =
                                        if (isCommandLine) commandTextStyle else bodyTextStyle

                                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                        OutputLineText(
                                            modifier = Modifier.lazySelectionItem(
                                                index = globalIndex,
                                                text = line.text,
                                                selectionState = selectionState,
                                                style = textStyle
                                            ),
                                            line = line,
                                            states = states,
                                            textStyle = textStyle
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
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

private data class CommandSection(
    val commandGlobalIndex: Int,
    val commandText: String,
    val lines: List<Pair<Int, OutputLine>>
)