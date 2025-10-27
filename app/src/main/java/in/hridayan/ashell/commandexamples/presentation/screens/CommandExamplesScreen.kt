@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.data.local.source.preloadedCommands
import `in`.hridayan.ashell.commandexamples.presentation.component.card.CommandExampleCard
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.AddCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.component.search.CustomSearchBar
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.appbar.TopAppBarLarge
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandExamplesScreen(viewModel: CommandViewModel = hiltViewModel()) {
    val weakHaptic = LocalWeakHaptic.current
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var splitButtonChecked by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (splitButtonChecked) 180f else 0f
    )
    val commands by viewModel.filteredCommands.collectAsState(initial = emptyList())
    val states by viewModel.states.collectAsState()

    Log.d("test", preloadedCommands.size.toString())

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
            SplitButtonLayout(
                modifier = Modifier.padding(bottom = 20.dp),
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(),
                        onClick = {
                            weakHaptic()
                            viewModel.clearInputFields()
                            isDialogOpen = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoResizeableText(text = stringResource(R.string.add))
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        checked = splitButtonChecked,
                        onCheckedChange = {
                            weakHaptic()
                            splitButtonChecked = it
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expand),
                            contentDescription = "Expand",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = rotation
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = splitButtonChecked,
                        onDismissRequest = { splitButtonChecked = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                AutoResizeableText(
                                    text = stringResource(R.string.load_predefined_commands),
                                    style = MaterialTheme.typography.bodySmallEmphasized
                                )
                            },
                            onClick = {
                                weakHaptic()
                                viewModel.loadDefaultCommands()
                            })
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(vertical = Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
        ) {
            item {
                CustomSearchBar(
                    value = states.search.query,
                    onValueChange = { it -> viewModel.onSearchQueryChange(it) },
                    onClearClick = { viewModel.onSearchQueryChange("") },
                    hint = stringResource(R.string.search_commands_here),
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(commands.size, key = { index -> commands[index].id }) { index ->
                CommandExampleCard(
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .animateItem(),
                    id = commands[index].id,
                    command = commands[index].command,
                    description = commands[index].description,
                    isFavourite = commands[index].isFavourite,
                    labels = commands[index].labels,
                )
            }
        }
    }

    if (isDialogOpen) AddCommandDialog(onDismiss = { isDialogOpen = false })
}