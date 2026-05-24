@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.item.settingsContent

@Composable
fun DarkThemeScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val controller = settingsViewModel.rememberController()
    val hapticsEnabled = LocalSettings.current.isHapticEnabled

    val listState = rememberLazyListState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.darkThemePage,
        listState = listState,
        headerItemCount = 0,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
    )

    val page = remember { settingsViewModel.darkThemePage }
    val resolvedGroups = page.resolveAll(highlightedKey = highlightedKey)

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.dark_theme),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier.fillMaxWidth().nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                settingsContent(
                    groups = resolvedGroups,
                    controller = controller,
                    hapticsEnabled = hapticsEnabled
                )

                item { Spacer(modifier = Modifier.fillMaxWidth().height(25.dp)) }
            }
        },
    )
}
