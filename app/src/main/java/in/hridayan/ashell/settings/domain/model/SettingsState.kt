package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.core.data.local.provider.SeedColor
import `in`.hridayan.ashell.core.domain.model.PaletteStyle

data class SettingsState(
    val isAutoUpdate: Boolean,
    val themeMode: Int,
    val isHighContrastDarkMode: Boolean,
    val seedColor: SeedColor,
    val paletteStyle: PaletteStyle,
    val isDynamicColor: Boolean,
    val isHapticEnabled: Boolean,
    val githubReleaseType: Int,
    val savedVersionCode: Int,
    val enableDirectDownload: Boolean,
    val localAdbMode: Int,
    val smoothScrolling: Boolean,
    val clearOutputConfirmation: Boolean,
    val overrideBookmarksLimit: Boolean,
    val disableSoftKeyboard: Boolean,
    val outputSaveDirectory: String,
    val saveWholeOutput: Boolean,
    val lastSavedFileUri: String,
    val isFirstLaunch: Boolean,
    val bookmarkSortType: Int,
    val commandsSortType: Int,
    val terminalFontStyle: Int,
    val isNewCommandsAvailable: Boolean,
    val lastLocalBackupTime: String,
    val lastCloudBackupTime: String,
    val lastLocalBackupType: String,
    val lastCloudBackupType: String,
    val selectedModelId: String,
    val aiCacheEnabled: Boolean,
    val aiCacheDays: Int,
    val screenDensityMultiplier: Float,
    val fontSizeMultiplier: Float,
    val fontFamily: Int
)