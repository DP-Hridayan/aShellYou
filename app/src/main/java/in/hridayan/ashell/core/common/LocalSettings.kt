package `in`.hridayan.ashell.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.GithubReleaseType
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SettingsState

val LocalSettings = compositionLocalOf {
    SettingsState(
        isAutoUpdate = false,
        themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        isHighContrastDarkMode = false,
        seedColor = SeedColor(SeedColorProvider.primary),
        paletteStyle = PaletteStyle.TONAL_SPOT,
        isDynamicColor = true,
        isHapticEnabled = true,
        githubReleaseType = GithubReleaseType.STABLE_GITHUB,
        savedVersionCode = 0,
        enableDirectDownload = true,
        localAdbMode = LocalAdbWorkingMode.BASIC,
        smoothScrolling = true,
        clearOutputConfirmation = true,
        overrideBookmarksLimit = false,
        disableSoftKeyboard = false,
        outputSaveDirectory = SettingsKeys.OUTPUT_SAVE_DIRECTORY.default,
        terminalFontStyle = TerminalFontStyle.MONOSPACE,
        saveWholeOutput = true,
        lastSavedFileUri = "",
        isFirstLaunch = true,
        bookmarkSortType = SortType.AZ,
        commandsSortType = SortType.AZ,
        isNewCommandsAvailable = true,
        lastLocalBackupTime = "",
        lastCloudBackupTime = "",
        lastCloudBackupType = "None",
        lastLocalBackupType = "None",
        selectedModelId = SettingsKeys.SELECTED_MODEL_ID.default,
        aiCacheEnabled = SettingsKeys.AI_CACHE_ENABLED.default,
        aiCacheDays = SettingsKeys.AI_CACHE_DAYS.default,
        screenDensityMultiplier = SettingsKeys.SCREEN_DENSITY_MULTIPLIER.default,
        fontSizeMultiplier = SettingsKeys.FONT_SIZE_MULTIPLIER.default,
        fontFamily = SettingsKeys.FONT_FAMILY.default
    )
}