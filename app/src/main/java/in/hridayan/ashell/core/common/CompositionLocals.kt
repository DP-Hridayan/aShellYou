package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.constants.SeedColors
import `in`.hridayan.ashell.core.utils.HapticUtils.strongHaptic
import `in`.hridayan.ashell.core.utils.HapticUtils.weakHaptic
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsState
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

val LocalWeakHaptic = staticCompositionLocalOf<() -> Unit> { {} }
val LocalStrongHaptic = staticCompositionLocalOf<() -> Unit> { {} }
val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}
val LocalSeedColor = staticCompositionLocalOf<Int> {
    error("No seed color provided")
}
val LocalTonalPalette = staticCompositionLocalOf<List<SeedColors>> {
    error("No tonal palette provided")
}

@Composable
fun CompositionLocals(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val autoUpdate by settingsViewModel.booleanState(SettingsKeys.AUTO_UPDATE)

    val themeMode by settingsViewModel.intState(SettingsKeys.THEME_MODE)

    val seedColor by settingsViewModel.intState(SettingsKeys.SEED_COLOR)

    val isDynamicColor by settingsViewModel.booleanState(SettingsKeys.DYNAMIC_COLORS)

    val isHighContrastDarkMode by settingsViewModel.booleanState(SettingsKeys.HIGH_CONTRAST_DARK_MODE)

    val isHapticEnabled by settingsViewModel.booleanState(SettingsKeys.HAPTICS_AND_VIBRATION)

    val githubReleaseType by settingsViewModel.intState(SettingsKeys.GITHUB_RELEASE_TYPE)

    val savedVersionCode by settingsViewModel.intState(SettingsKeys.SAVED_VERSION_CODE)

    val enableDirectDownload by settingsViewModel.booleanState(SettingsKeys.ENABLE_DIRECT_DOWNLOAD)

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
            )
        }

    val isDarkTheme = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val tonalPalette = listOf<SeedColors>(
        SeedColors.Blue,
        SeedColors.Indigo,
        SeedColors.Purple,
        SeedColors.Pink,
        SeedColors.Red,
        SeedColors.Orange,
        SeedColors.Yellow,
        SeedColors.Teal,
        SeedColors.Green
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

