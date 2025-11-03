@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.shell.presentation.model.ShellState
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun UtilityButtonGroup(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel(),
    showClearOutputDialog: () -> Unit = {},
    handleClearOutput: () -> Unit = {},
    showBookmarkDialog: () -> Unit = {},
    showHistoryMenu: () -> Unit = {},
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val focusManager = LocalFocusManager.current
    val navController = LocalNavController.current
    val screenDensity = LocalDensity.current
    val interactionSources = remember { List(5) { MutableInteractionSource() } }
    val askToClean = LocalSettings.current.clearOutputConfirmation
    val states by shellViewModel.states.collectAsState()
    val utilityRowPadding = PaddingValues(top = 30.dp, bottom = 25.dp, start = 20.dp, end = 20.dp)

    if (states.search.isVisible && !states.output.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(states.buttonGroupHeight + 55.dp),
            contentAlignment = Alignment.Center
        ) {
            CustomSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 16.dp),
                value = states.search.textFieldValue,
                onValueChange = { it -> shellViewModel.onSearchQueryChange(it) },
                hint = stringResource(R.string.search_commands_here),
                trailingIcon = {
                    if (states.search.textFieldValue.text.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear),
                            contentDescription = "Clear text",
                            modifier = Modifier
                                .clickable(
                                    enabled = true,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        weakHaptic()
                                        shellViewModel.onSearchQueryChange(TextFieldValue(""))
                                        focusManager.clearFocus()
                                    }
                                )
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_cross),
                        contentDescription = "Clear text",
                        modifier = Modifier
                            .clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    weakHaptic()
                                    shellViewModel.toggleSearchBar()
                                }
                            )
                    )
                }
            )
        }
    } else
        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = modifier
                .fillMaxWidth()
                .padding(utilityRowPadding)
                .onGloballyPositioned { layoutCoordinates ->
                    val height = with(screenDensity) {
                        layoutCoordinates.size.height.toDp()
                    }
                    shellViewModel.updateButtonGroupHeight(height)
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    weakHaptic()
                    if (states.output.isEmpty()) {
                        showToast(context, context.getString(R.string.nothing_to_search))
                    } else {
                        shellViewModel.toggleSearchBar()
                    }
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

                    if (states.output.isEmpty()) {
                        showToast(context, context.getString(R.string.nothing_to_clear))
                        return@IconButton
                    }

                    if (states.shellState == ShellState.Busy) {
                        showToast(context, context.getString(R.string.abort_command))
                        return@IconButton
                    }

                    if (askToClean) showClearOutputDialog()
                    else handleClearOutput()
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
                    navController.navigate(NavRoutes.SettingsScreen)
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