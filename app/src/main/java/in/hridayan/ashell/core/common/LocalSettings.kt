package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.hridayan.ashell.core.common.constants.GithubReleaseType
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.constants.SeedColors
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
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
        localAdbMode = LocalAdbWorkingMode.BASIC,
        smoothScrolling = true,
        clearOutputConfirmation = true,
        overrideBookmarksLimit = false,
        disableSoftKeyboard = false,
        outputSaveDirectory = SettingsKeys.OUTPUT_SAVE_DIRECTORY.default as String,
        saveWholeOutput = true,
        lastSavedFileUri = "",
        isFirstLaunch = true,
        bookmarkSortType = SortType.AZ
    )
}