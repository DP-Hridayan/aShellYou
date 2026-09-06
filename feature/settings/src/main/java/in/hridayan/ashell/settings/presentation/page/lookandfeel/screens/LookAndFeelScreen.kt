@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawDreamWorld
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.FontStyleBottomSheet
import `in`.hridayan.ashell.settings.presentation.components.dialog.PaletteStylePickerDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.components.tab.ColorTabs
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel
import `in`.hridayan.settingsdsl.ui.SettingsColumn

private const val ITEM_KEY_HEADER_IMAGE = "header_image"
private const val ITEM_KEY_COLOR_TABS = "color_tabs"

@Composable
fun LookAndFeelScreen(
    modifier: Modifier = Modifier,
    lookAndFeelViewModel: LookAndFeelViewModel = hiltViewModel(),
) {
    val dialogManager = LocalDialogManager.current
    val navController = LocalNavController.current
    val currentPaletteStyle = LocalPaletteStyle.current
    val settings = LocalSettings.current
    val themeMode = settings[SettingsKeys.ThemeMode]
    val isDarkMode = LocalDarkMode.current
    val autoDarkModeOnBatterySaver = settings[SettingsKeys.AutoDarkModeOnBatterySaver]
    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val isDynamicColorEnabled = settings[SettingsKeys.DynamicColors]
    val autoScaleUI = settings[SettingsKeys.AutoScaleUi]
    val userGeneratedColorSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]
    val isCustomColorSchemeDarkThemed = settings[SettingsKeys.IsCustomColorSchemeDarkThemed]

    var showFontStyleBottomSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.look_and_feel),
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
                item(ITEM_KEY_HEADER_IMAGE) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp, vertical = 25.dp),
                        imageVector = DynamicColorImageVectors.undrawDreamWorld(),
                        contentDescription = null,
                    )
                }

                item(ITEM_KEY_COLOR_TABS) {
                    ColorTabs(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        onClickTab = { seedColor ->
                            lookAndFeelViewModel.setSeedColor(seedColor)
                            if (isDynamicColorEnabled) lookAndFeelViewModel.disableDynamicColors()
                            if (userGeneratedColorSchemeApplied) lookAndFeelViewModel.disableUserGeneratedColorScheme()
                        },
                        onClickMonochromeTab = {
                            if (isDynamicColorEnabled) lookAndFeelViewModel.disableDynamicColors()
                            if (userGeneratedColorSchemeApplied) lookAndFeelViewModel.disableUserGeneratedColorScheme()
                        },
                        onClickCreateTheme = {
                            navController.navigate(NavRoutes.GenerateColorSchemeScreen)
                        }
                    )
                }

                group {
                    switchItem(SettingsKeys.DynamicColors) {
                        title(R.string.dynamic_colors)
                        description(R.string.des_dynamic_colors)
                        icon(R.drawable.ic_dynamic_color)
                        visible { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                    }

                    clickableItem(SettingsKeys.PaletteStyle) {
                        title(R.string.palette_style)
                        description { stringResource(currentPaletteStyle.displayNameResId) }
                        icon(R.drawable.ic_styles)
                        visible { !(isDynamicColorEnabled || userGeneratedColorSchemeApplied) }
                        onClick { dialogManager.show(SettingsDialogKey.PaletteStyle) }
                    }

                    clickableItem(SettingsKeys.DarkTheme) {
                        title(R.string.dark_theme)
                        description {
                            when {
                                autoDarkModeOnBatterySaver && isDarkMode -> stringResource(R.string.on)

                                userGeneratedColorSchemeApplied && !isDynamicColorEnabled -> {
                                    if (isCustomColorSchemeDarkThemed) {
                                        stringResource(R.string.on)
                                    } else {
                                        stringResource(R.string.off)
                                    }
                                }

                                themeMode == AppCompatDelegate.MODE_NIGHT_YES -> stringResource(R.string.on)
                                themeMode == AppCompatDelegate.MODE_NIGHT_NO -> stringResource(R.string.off)
                                themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> stringResource(
                                    R.string.system
                                )

                                else -> ""
                            }
                        }
                        icon(if (isDarkMode) Icons.Outlined.DarkMode else Icons.Rounded.LightMode)
                        visible { !userGeneratedColorSchemeApplied || isDynamicColorEnabled }
                        onClick { navController.navigate(NavRoutes.DarkThemeScreen) }
                    }
                }

                group(R.string.font_family) {
                    clickableItem(SettingsKeys.FontFamily) {
                        title(R.string.font_family)
                        description(R.string.des_font_family)
                        icon(Icons.Rounded.TextFields)
                        onClick { showFontStyleBottomSheet = true }
                    }
                }

                group(R.string.ui_scale) {
                    switchItem(SettingsKeys.AutoScaleUi) {
                        title(R.string.auto_scale_ui)
                        description(R.string.des_auto_scale_ui)
                        icon(R.drawable.ic_transform)
                        experimentalFlagText(R.string.experimental)
                    }

                    clickableItem(SettingsKeys.CustomUiScale) {
                        title(R.string.custom_ui_scale)
                        description(R.string.des_ui_scale)
                        icon(R.drawable.ic_high_density)
                        enabled(!autoScaleUI)
                        onClick { navController.navigate(NavRoutes.UiScaleScreen) }
                    }
                }


                group(R.string.additional_settings) {
                    switchItem(SettingsKeys.HapticsAndVibration) {
                        title(R.string.haptics_and_vibration)
                        description(R.string.des_haptics_and_vibration)
                        icon(R.drawable.ic_vibration)
                    }

                    clickableItem(SettingsKeys.Language) {
                        title(R.string.default_language)
                        description(R.string.des_default_language)
                        icon(R.drawable.ic_language)
                        onClick { navController.navigate(NavRoutes.LanguagesScreen) }
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

    SettingsDialogKey.PaletteStyle.createDialog { dm ->
        PaletteStylePickerDialog(
            onDismiss = { dm.dismiss() },
            onConfirm = { style ->
                lookAndFeelViewModel.setPaletteStyle(style)
            }
        )
    }

    if (showFontStyleBottomSheet) {
        FontStyleBottomSheet(
            onDismiss = { showFontStyleBottomSheet = false }
        )
    }
}

