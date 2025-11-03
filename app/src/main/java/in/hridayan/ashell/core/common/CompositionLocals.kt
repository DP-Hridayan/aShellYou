@file:OptIn(ExperimentalSharedTransitionApi::class)

package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.data.provider.AppSeedColors
import `in`.hridayan.ashell.core.data.provider.SeedColor
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.strongHaptic
import `in`.hridayan.ashell.core.presentation.utils.HapticUtils.weakHaptic
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsState
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

val LocalWeakHaptic = staticCompositionLocalOf { {} }
val LocalStrongHaptic = staticCompositionLocalOf { {} }
val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}
val LocalSeedColor = staticCompositionLocalOf<SeedColor> {
    error("No seed color provided")
}
val LocalTonalPalette = staticCompositionLocalOf<List<AppSeedColors>> {
    error("No tonal palette provided")
}

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    error("No shared transition scope provided")
}

val LocalAnimatedContentScope = staticCompositionLocalOf<AnimatedContentScope> {
    error("No AnimatedContentScope provided")
}

@Composable
fun CompositionLocals(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val autoUpdate by settingsViewModel.booleanState(SettingsKeys.AUTO_UPDATE)

    val themeMode by settingsViewModel.intState(SettingsKeys.THEME_MODE)

    val primarySeed by settingsViewModel.intState(SettingsKeys.PRIMARY_SEED)

    val secondarySeed by settingsViewModel.intState(SettingsKeys.SECONDARY_SEED)

    val tertiarySeed by settingsViewModel.intState(SettingsKeys.TERTIARY_SEED)

    val seedColor = SeedColor(primarySeed, secondarySeed, tertiarySeed)

    val isDynamicColor by settingsViewModel.booleanState(SettingsKeys.DYNAMIC_COLORS)

    val isHighContrastDarkMode by settingsViewModel.booleanState(SettingsKeys.HIGH_CONTRAST_DARK_MODE)

    val isHapticEnabled by settingsViewModel.booleanState(SettingsKeys.HAPTICS_AND_VIBRATION)

    val githubReleaseType by settingsViewModel.intState(SettingsKeys.GITHUB_RELEASE_TYPE)

    val savedVersionCode by settingsViewModel.intState(SettingsKeys.SAVED_VERSION_CODE)

    val enableDirectDownload by settingsViewModel.booleanState(SettingsKeys.ENABLE_DIRECT_DOWNLOAD)

    val smoothScrolling by settingsViewModel.booleanState(SettingsKeys.SMOOTH_SCROLLING)

    val clearOutputConfirmation by settingsViewModel.booleanState(SettingsKeys.CLEAR_OUTPUT_CONFIRMATION)

    val overrideBookmarksLimit by settingsViewModel.booleanState(SettingsKeys.OVERRIDE_MAXIMUM_BOOKMARKS_LIMIT)

    val disableSoftKeyboard by settingsViewModel.booleanState(SettingsKeys.DISABLE_SOFT_KEYBOARD)

    val localAdbMode by settingsViewModel.intState(SettingsKeys.LOCAL_ADB_WORKING_MODE)

    val outputSaveDirectory by settingsViewModel.stringState(SettingsKeys.OUTPUT_SAVE_DIRECTORY)

    val saveWholeOutput by settingsViewModel.booleanState(SettingsKeys.SAVE_WHOLE_OUTPUT)

    val lastSavedFileUri by settingsViewModel.stringState(SettingsKeys.LAST_SAVED_FILE_URI)

    val isFirstLaunch by settingsViewModel.booleanState(SettingsKeys.FIRST_LAUNCH)

    val bookmarkSortType by settingsViewModel.intState(SettingsKeys.BOOKMARK_SORT_TYPE)

    val commandSortType by settingsViewModel.intState(SettingsKeys.COMMAND_SORT_TYPE)

    val terminalFontStyle by settingsViewModel.intState(SettingsKeys.TERMINAL_FONT_STYLE)

    val state =
        remember(
            autoUpdate,
            themeMode,
            seedColor,
            isDynamicColor,
            isHighContrastDarkMode,
            isHapticEnabled,
            githubReleaseType,
            savedVersionCode,
            enableDirectDownload,
            localAdbMode,
            smoothScrolling,
            clearOutputConfirmation,
            overrideBookmarksLimit,
            disableSoftKeyboard,
            outputSaveDirectory,
            saveWholeOutput,
            lastSavedFileUri,
            isFirstLaunch,
            bookmarkSortType,
            commandSortType,
            terminalFontStyle
        ) {
            SettingsState(
                isAutoUpdate = autoUpdate,
                themeMode = themeMode,
                isHighContrastDarkMode = isHighContrastDarkMode,
                seedColor = seedColor,
                isDynamicColor = isDynamicColor,
                isHapticEnabled = isHapticEnabled,
                githubReleaseType = githubReleaseType,
                savedVersionCode = savedVersionCode,
                enableDirectDownload = enableDirectDownload,
                localAdbMode = localAdbMode,
                smoothScrolling = smoothScrolling,
                clearOutputConfirmation = clearOutputConfirmation,
                overrideBookmarksLimit = overrideBookmarksLimit,
                disableSoftKeyboard = disableSoftKeyboard,
                outputSaveDirectory = outputSaveDirectory,
                saveWholeOutput = saveWholeOutput,
                lastSavedFileUri = lastSavedFileUri,
                isFirstLaunch = isFirstLaunch,
                bookmarkSortType = bookmarkSortType,
                commandsSortType = commandSortType,
                terminalFontStyle = terminalFontStyle
            )
        }

    val isDarkTheme = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val tonalPalette = listOf(
        AppSeedColors.Color01,
        AppSeedColors.Color02,
        AppSeedColors.Color03,
        AppSeedColors.Color04,
        AppSeedColors.Color05,
        AppSeedColors.Color06,
        AppSeedColors.Color07,
        AppSeedColors.Color08,
        AppSeedColors.Color09,
        AppSeedColors.Color10,
        AppSeedColors.Color11,
        AppSeedColors.Color12,
        AppSeedColors.Color13,
        AppSeedColors.Color14,
        AppSeedColors.Color15,
        AppSeedColors.Color16,
        AppSeedColors.Color17,
        AppSeedColors.Color18,
        AppSeedColors.Color19,
        AppSeedColors.Color20
    )

    val weakHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.weakHaptic()
            }
        }
    }

    val strongHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.strongHaptic()
            }
        }
    }

    CompositionLocalProvider(
        LocalSettings provides state,
        LocalWeakHaptic provides weakHaptic,
        LocalStrongHaptic provides strongHaptic,
        LocalSeedColor provides seedColor,
        LocalDarkMode provides isDarkTheme,
        LocalTonalPalette provides tonalPalette,
    ) {
        content()
    }
}

@Composable
private fun SettingsViewModel.booleanState(key: SettingsKeys): State<Boolean> {
    return getBoolean(key).collectAsState(initial = key.default as Boolean)
}

@Composable
private fun SettingsViewModel.intState(key: SettingsKeys): State<Int> {
    return getInt(key).collectAsState(initial = key.default as Int)
}

@Composable
private fun SettingsViewModel.floatState(key: SettingsKeys): State<Float> {
    return getFloat(key).collectAsState(initial = key.default as Float)
}

@Composable
private fun SettingsViewModel.stringState(key: SettingsKeys): State<String> {
    return getString(key).collectAsState(initial = key.default as String)
}

