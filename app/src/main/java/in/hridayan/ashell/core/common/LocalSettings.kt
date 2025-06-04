package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.hridayan.ashell.core.common.constants.GithubReleaseType
import `in`.hridayan.ashell.core.common.constants.SeedColors
import `in`.hridayan.ashell.core.common.constants.SubjectCardStyle
import `in`.hridayan.ashell.settings.domain.model.SettingsState

val LocalSettings = compositionLocalOf<SettingsState> {
    SettingsState(
        isAutoUpdate = false,
        themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        isHighContrastDarkMode = false,
        seedColor = SeedColors.Blue.seed,
        isDynamicColor = true,
        isHapticEnabled = true,
        githubReleaseType = GithubReleaseType.STABLE,
        savedVersionCode = 0,
        enableDirectDownload = true,
    )
}