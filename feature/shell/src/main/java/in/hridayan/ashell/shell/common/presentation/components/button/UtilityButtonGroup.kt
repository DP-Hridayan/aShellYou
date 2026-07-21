@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.common.presentation.components.button

import `in`.hridayan.ashell.core.resources.R


import `in`.hridayan.ashell.core.common.LocalSettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupIconButtonDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.shell.common.presentation.model.ShellState
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel
import `in`.hridayan.ashell.core.common.SettingsKeys

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
    val res = LocalResources.current
    val focusManager = LocalFocusManager.current
    val navController = LocalNavController.current
    val screenDensity = LocalDensity.current
    val askToClean = LocalSettings.current[SettingsKeys.ClearOutputConfirmation]
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
                onValueChange = { shellViewModel.onSearchQueryChange(it) },
                hint = stringResource(R.string.search_output),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
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
                                    onClick = withHaptic {
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
                                onClick = withHaptic {
                                    shellViewModel.toggleSearchBar()
                                }
                            )
                    )
                }
            )
        }
    } else {
        val iconButtonConfig = ButtonGroupIconButtonDefaults.defaultConfig(
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        val buttonGroupItems = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    type = ButtonType.IconButton,
                    iconButtonConfig = iconButtonConfig
                ),
                icon = Icons.Rounded.Search,
                onClick = {
                    if (states.output.isEmpty()) {
                        showToast(context, res.getString(R.string.nothing_to_search))
                    } else {
                        shellViewModel.toggleSearchBar()
                    }
                }
            ),
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    type = ButtonType.IconButton,
                    iconButtonConfig = iconButtonConfig
                ),
                iconResId = R.drawable.ic_bookmarks,
                onClick = { showBookmarkDialog() }
            ),
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    type = ButtonType.IconButton,
                    iconButtonConfig = iconButtonConfig
                ),
                icon = Icons.Rounded.History,
                onClick = { showHistoryMenu() }
            ),
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    type = ButtonType.IconButton,
                    iconButtonConfig = iconButtonConfig
                ),
                iconResId = R.drawable.ic_clear,
                onClick = {
                    if (states.output.isEmpty()) {
                        showToast(context, res.getString(R.string.nothing_to_clear))
                        return@ButtonGroupItem
                    }

                    if (states.shellState == ShellState.Busy) {
                        showToast(context, res.getString(R.string.abort_command))
                        return@ButtonGroupItem
                    }

                    if (askToClean) showClearOutputDialog()
                    else handleClearOutput()
                }
            ),
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    type = ButtonType.IconButton,
                    iconButtonConfig = iconButtonConfig
                ),
                iconResId = R.drawable.ic_settings,
                onClick = { navController.navigate(NavRoutes.SettingsScreen()) }
            ),
        )

        OverflowButtonGroup(
            items = buttonGroupItems,
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
        )
    }
}
