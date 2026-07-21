@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.behavior.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.navigateBack
import `in`.hridayan.ashell.settings.presentation.components.dialog.ConfigureSaveDirectoryDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent

@Composable
fun BehaviorScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val controller = settingsViewModel.rememberController()

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowDialog -> dialogManager.show(event.key)
                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.behaviorPage,
        listState = listState,
        headerItemCount = 0,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
        topAppBarState = topAppBarState,
    )

    val page = remember { settingsViewModel.behaviorPage }
    val resolvedGroups = page.resolveAll(highlightedKey = highlightedKey)

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.behavior),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                settingsContent(
                    groups = resolvedGroups,
                    controller = controller,
                    hapticsEnabled = hapticsEnabled
                )

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )

    SettingsDialogKey.ConfigureSaveDir.createDialog {
        ConfigureSaveDirectoryDialog(onDismiss = { it.dismiss() })
    }
}
