@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.SettingsScreen
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun UtilityButtonGroup(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel(),
    isOutputEmpty: Boolean,
    showClearOutputDialog: () -> Unit = {},
    handleClearOutput: () -> Unit = {},
    showBookmarkDialog: () -> Unit = {},
    showHistoryMenu: () -> Unit = {},
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val interactionSources = remember { List(5) { MutableInteractionSource() } }
    val askToClean = LocalSettings.current.clearOutputConfirmation
    val shellState by shellViewModel.shellState.collectAsState()
    val searchQuery = shellViewModel.searchQuery.collectAsState()
    val isSearchVisible = shellViewModel.isSearchBarVisible.collectAsState()
    val searchBarState = rememberSearchBarState()

    if (isSearchVisible.value && !isOutputEmpty) {
        val inputField = @Composable {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { shellViewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_output)) },
                trailingIcon = {
                    IconButton(onClick = { shellViewModel.toggleSearchBar() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search")
                    }
                }
            )
        }

        SearchBar(state = searchBarState, inputField = inputField)
    } else
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
                    shellViewModel.toggleSearchBar()
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