@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.data.local.source.preloadedCommands
import `in`.hridayan.ashell.commandexamples.presentation.component.card.CommandExampleCard
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.AddCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.CommandsSortDialog
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.appbar.TopAppBarLarge
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible

@SuppressLint("RememberInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandExamplesScreen(viewModel: CommandExamplesViewModel = hiltViewModel()) {
    val weakHaptic = LocalWeakHaptic.current
    val focusManager = LocalFocusManager.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val listState =rememberLazyListState()

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showAddCommandDialog by rememberSaveable { mutableStateOf(false) }
    var showSortExamplesDialog by rememberSaveable { mutableStateOf(false) }
    val focusRequester = FocusRequester()
    val sortType = LocalSettings.current.commandsSortType
    val commands by viewModel.filteredCommands.collectAsState()

    val states by viewModel.states.collectAsState()
    val isKeyboardVisible = isKeyboardVisible()

    val addOptions = listOf(
        stringResource(R.string.load_predefined_commands) to { viewModel.loadDefaultCommands() },
        stringResource(R.string.add_new_item) to {
            viewModel.clearInputFields()
            showAddCommandDialog = true
        }
    )

    Log.d("test", preloadedCommands.size.toString())

    LaunchedEffect(sortType) {
        viewModel.setSortType(sortType)
    }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBarLarge(
                topBarTitle = stringResource(id = R.string.commands),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier
                            .semantics {
                                traversalIndex = -1f
                                stateDescription =
                                    if (fabMenuExpanded) "Expanded" else "Collapsed"
                                contentDescription = "Toggle menu"
                            }
                            .animateFloatingActionButton(
                                visible = !isKeyboardVisible.value,
                                alignment = Alignment.BottomEnd
                            )
                            .focusRequester(focusRequester),
                        checked = fabMenuExpanded,
                        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                            initialColor = MaterialTheme.colorScheme.tertiaryContainer,
                            finalColor = MaterialTheme.colorScheme.tertiary
                        ),
                        onCheckedChange = {
                            weakHaptic()
                            fabMenuExpanded = !fabMenuExpanded
                        }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Rounded.Close else Icons.Rounded.Add
                            }
                        }

                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon(
                                checkedProgress = { checkedProgress },
                                color = ToggleFloatingActionButtonDefaults.iconColor(
                                    initialColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    finalColor = MaterialTheme.colorScheme.onTertiary
                                )
                            )
                        )
                    }
                }
            ) {
                addOptions.forEachIndexed { i, option ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            weakHaptic()
                            fabMenuExpanded = false
                            option.second()
                        },
                        text = { AutoResizeableText(option.first) },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        icon = { }
                    )
                }

            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = PaddingValues(vertical = Dimens.paddingMedium),
            ) {
                item {
                    CustomSearchBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        value = states.search.textFieldValue,
                        onValueChange = { it -> viewModel.onSearchQueryChange(it) },
                        hint = stringResource(R.string.search_commands_here),
                        trailingIcon = {
                            if (states.search.textFieldValue.text.isNotEmpty()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clear),
                                    contentDescription = "Clear text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .clickable(
                                            enabled = true,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                weakHaptic()
                                                viewModel.onSearchQueryChange(TextFieldValue(""))
                                                focusManager.clearFocus()
                                            }
                                        )
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sort),
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .clickable(
                                            enabled = true,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                weakHaptic()
                                                showSortExamplesDialog = true
                                            }
                                        )
                                )
                            }
                        }
                    )
                }

                items(commands.size, key = { index -> commands[index].id }) { index ->
                    CommandExampleCard(
                        modifier = Modifier
                            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
                            .animateItem(),
                        id = commands[index].id,
                        command = commands[index].command,
                        description = commands[index].description,
                        isFavourite = commands[index].isFavourite,
                        labels = commands[index].labels,
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }

            if (states.search.textFieldValue.text.isNotEmpty() && commands.isEmpty()) {
                NoSearchResultUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }

    if (showAddCommandDialog) AddCommandDialog(onDismiss = { showAddCommandDialog = false })
    if (showSortExamplesDialog) CommandsSortDialog(onDismiss = { showSortExamplesDialog = false })
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
