@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.emptyPreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.ui.SettingsColumn

@Composable
fun DarkThemeScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val prefs by settingsViewModel.preferences.collectAsState(initial = emptyPreferences())
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.dark_theme),
        content = { innerPadding, topBarScrollBehavior ->
            SettingsColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                contentPadding = innerPadding,
                topAppBarState = topAppBarState,
                hapticsEnabled = hapticsEnabled,
            ) {
                group(R.string.preference) {
                    radioGroupItem(SettingsKeys.ThemeMode) {
                        options(RadioGroupOptionsProvider.darkModeOptions)
                        onIntChanged { key, value ->
                            @Suppress("UNCHECKED_CAST")
                            settingsViewModel.setInt(key as SettingsKeys<Int>, value)
                        }
                    }
                }

                group(R.string.battery_saver) {
                    switchItem(SettingsKeys.AutoDarkModeOnBatterySaver) {
                        title(R.string.auto_dark_mode)
                        description(R.string.des_auto_dark_mode)
                        icon(R.drawable.ic_night_sight_auto)
                    }
                }

                group(R.string.additional_settings) {
                    switchItem(SettingsKeys.HighContrastDarkMode) {
                        title(R.string.high_contrast_dark_mode)
                        description(R.string.des_high_contrast_dark_mode)
                        icon(R.drawable.ic_amoled_theme)
                    }
                }

            }
        },
    )
}
