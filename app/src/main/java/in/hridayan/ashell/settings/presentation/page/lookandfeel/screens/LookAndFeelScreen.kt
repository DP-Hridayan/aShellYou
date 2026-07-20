@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.twotone.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.settings.presentation.components.svg.vectors.themePicker
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.FontStyleBottomSheet
import `in`.hridayan.ashell.settings.presentation.components.dialog.PaletteStylePickerDialog
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.components.tab.ColorTabs
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey

@Composable
fun LookAndFeelScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    lookAndFeelViewModel: LookAndFeelViewModel = hiltViewModel(),
) {
    val dialogManager = LocalDialogManager.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val controller = settingsViewModel.rememberController()
    val currentPaletteStyle = LocalPaletteStyle.current
    val settings = LocalSettings.current
    val themeMode = settings[SettingsKeys.ThemeMode]
    val isDarkMode = LocalDarkMode.current
    val autoDarkModeOnBatterySaver = settings[SettingsKeys.AutoDarkModeOnBatterySaver]
    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val isDynamicColorEnabled = settings[SettingsKeys.DynamicColors]
    val autoScaleUI = settings[SettingsKeys.AutoScaleUi]

    var showFontStyleBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.LaunchIntent -> context.startActivity(event.intent)
                is SettingsUiEvent.Navigate -> navController.navigate(event.route)
                is SettingsUiEvent.ShowDialog -> dialogManager.show(event.key)
                SettingsUiEvent.ShowFontStylesBottomSheet -> showFontStyleBottomSheet = true
                else -> {}
            }
        }
    }

    SettingsDialogKey.PaletteStyle.createDialog { dm ->
        PaletteStylePickerDialog(
            onDismiss = { dm.dismiss() },
            onConfirm = { style ->
                lookAndFeelViewModel.setPaletteStyle(style)
                lookAndFeelViewModel.disableDynamicColors()
            }
        )
    }

    if (showFontStyleBottomSheet) {
        FontStyleBottomSheet(
            onDismiss = { showFontStyleBottomSheet = false }
        )
    }

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.lookAndFeelPage,
        listState = listState,
        headerItemCount = 2,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
        topAppBarState = topAppBarState,
    )

    val page = remember { settingsViewModel.lookAndFeelPage }

    val resolvedGroups = page.resolveAll(
        highlightedKey = highlightedKey,
        enabledOverrides = mapOf(
            SettingsKeys.CustomUiScale to { !autoScaleUI }
        ),
        descriptionOverrides = mapOf(
            SettingsKeys.PaletteStyle to { stringResource(currentPaletteStyle.displayNameResId) },
            SettingsKeys.DarkTheme to {
                when {
                    autoDarkModeOnBatterySaver && isDarkMode -> stringResource(R.string.on)
                    themeMode == AppCompatDelegate.MODE_NIGHT_YES -> stringResource(R.string.on)
                    themeMode == AppCompatDelegate.MODE_NIGHT_NO -> stringResource(R.string.off)
                    themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> stringResource(R.string.system)
                    else -> ""
                }
            }
        ),
        iconOverrides = mapOf(
            SettingsKeys.DarkTheme to {
                if (isDarkMode) Icons.TwoTone.DarkMode else Icons.Rounded.LightMode
            }
        ),
        visibilityOverrides = mapOf(
            SettingsKeys.PaletteStyle to { !isDynamicColorEnabled }
        ))

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.look_and_feel),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                item {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 100.dp, vertical = 25.dp),
                        imageVector = DynamicColorImageVectors.themePicker(),
                        contentDescription = null,
                    )
                }

                item {
                    ColorTabs(
                        modifier = Modifier.padding(20.dp),
                        onClickTab = { seedColor ->
                            lookAndFeelViewModel.setSeedColor(seedColor)
                            lookAndFeelViewModel.disableDynamicColors()
                        },
                        onClickMonochromeTab = {
                            lookAndFeelViewModel.disableDynamicColors()
                        }
                    )
                }

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
}
