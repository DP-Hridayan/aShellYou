@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.behavior.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.UnfoldMoreDouble
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.dialog.ConfigureSaveDirectoryDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.ui.SettingsColumn

@Composable
fun BehaviorScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.behavior),
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
                group(R.string.local_adb_shell) {
                    radioGroupItem(SettingsKeys.LocalAdbWorkingMode) {
                        options(RadioGroupOptionsProvider.localAdbShellModeOptions)
                        onIntChanged { key, value ->
                            @Suppress("UNCHECKED_CAST")
                            settingsViewModel.setInt(key as SettingsKeys<Int>, value)
                        }
                    }
                }

                group(R.string.launch) {
                    switchItem(SettingsKeys.DefaultLaunchIsLocalAdb) {
                        title(R.string.set_local_adb_as_default_launch)
                        description(R.string.des_set_local_adb_as_default_launch)
                        icon(R.drawable.ic_rocket_launch)
                    }
                }

                group(R.string.terminal) {
                    switchItem(SettingsKeys.SmoothScrolling) {
                        title(R.string.smooth_scrolling)
                        description(R.string.des_smooth_scroll)
                        icon(Icons.Rounded.UnfoldMoreDouble)
                    }

                    switchItem(SettingsKeys.ClearOutputConfirmation) {
                        title(R.string.clear_output_confirmation)
                        description(R.string.des_clear_output_confirmation)
                        icon(R.drawable.ic_clear)
                    }

                    switchItem(SettingsKeys.OverrideMaximumBookmarksLimit) {
                        title(R.string.override_bookmarks_limit)
                        description(R.string.des_override_bookmarks)
                        icon(R.drawable.ic_bookmarks)
                    }

                    switchItem(SettingsKeys.DisableSoftKeyboard) {
                        title(R.string.disable_softkey)
                        description(R.string.des_disable_softkey)
                        icon(R.drawable.ic_disable_keyboard)
                    }
                }

                group(R.string.terminal_font_style) {
                    buttonGroupItem(SettingsKeys.TerminalFontStyle) {
                        options(
                            ButtonGroupOption(TerminalFontStyle.MONOSPACE, R.string.monospace),
                            ButtonGroupOption(TerminalFontStyle.SYSTEM_FONT, R.string.system_font),
                        )
                        onIntChanged { key, value ->
                            @Suppress("UNCHECKED_CAST")
                            settingsViewModel.setInt(key as SettingsKeys<Int>, value)
                        }
                    }
                }

                group(R.string.file_actions) {
                    clickableItem(SettingsKeys.OutputSaveDirectory) {
                        title(R.string.configure_save_directory)
                        description(R.string.des_configure_save_directory)
                        icon(R.drawable.ic_directory)
                        onClick { dialogManager.show(SettingsDialogKey.ConfigureSaveDir) }
                    }

                    switchItem(SettingsKeys.SaveWholeOutput) {
                        title(R.string.save_whole_output)
                        description(R.string.des_save_whole_output)
                        icon(R.drawable.ic_save_as)
                    }
                }

                item(key = "spacer_bottom") {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )

    SettingsDialogKey.ConfigureSaveDir.createDialog { dm ->
        ConfigureSaveDirectoryDialog(onDismiss = { dm.dismiss() })
    }
}
