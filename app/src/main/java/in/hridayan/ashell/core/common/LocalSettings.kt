package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.hridayan.ashell.core.domain.model.GithubReleaseType
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsState

val LocalSettings = compositionLocalOf {
    SettingsState(
        isAutoUpdate = false,
        themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        isHighContrastDarkMode = false,
        seedColor = SeedColorProvider.seed,
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
        terminalFontStyle = TerminalFontStyle.MONOSPACE,
        saveWholeOutput = true,
        lastSavedFileUri = "",
        isFirstLaunch = true,
        bookmarkSortType = SortType.AZ,
        commandsSortType = SortType.AZ
    )
}