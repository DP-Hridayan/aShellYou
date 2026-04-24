package `in`.hridayan.ashell.settings.presentation.model

data class SettingsUiState(
    val isFirstLaunch: Boolean? = null,
    val settingsPageList: List<PreferenceGroup> = emptyList(),
    val lookAndFeelPageList: List<PreferenceGroup> = emptyList(),
    val darkThemePageList: List<PreferenceGroup> = emptyList(),
    val aboutPageList: List<PreferenceGroup> = emptyList(),
    val autoUpdatePageList: List<PreferenceGroup> = emptyList(),
    val behaviorPageList: List<PreferenceGroup> = emptyList(),
    val backupPageList: List<PreferenceGroup> = emptyList(),
    val backupSchedulerPageList: List<PreferenceGroup> = emptyList(),
)
