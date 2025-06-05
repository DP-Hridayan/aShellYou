package `in`.hridayan.ashell.settings.domain.model

data class SettingsState(
    val isAutoUpdate: Boolean,
    val themeMode: Int,
    val isHighContrastDarkMode: Boolean,
    val seedColor: Int,
    val isDynamicColor: Boolean,
    val isHapticEnabled: Boolean,
    val githubReleaseType: Int,
    val savedVersionCode: Int,
    val enableDirectDownload: Boolean,
    val localAdbMode :Int,
    val smoothScrolling : Boolean,
    val clearOutputConfirmation : Boolean,
    val overrideBookmarksLimit : Boolean,
    val disableSoftKeyboard : Boolean,
    val outputSaveDirectory : String,
    val saveWholeOutput : Boolean,
)