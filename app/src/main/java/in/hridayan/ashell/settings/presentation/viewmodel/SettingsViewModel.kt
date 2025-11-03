package `in`.hridayan.ashell.settings.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.BackupOption
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.domain.usecase.ToggleSettingUseCase
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val toggleSettingUseCase: ToggleSettingUseCase,
) : ViewModel() {
    var isFirstLaunch by mutableStateOf<Boolean?>(null)
        private set

    var settingsPageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var lookAndFeelPageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var darkThemePageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var aboutPageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var autoUpdatePageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var behaviorPageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    var backupPageList by mutableStateOf<List<PreferenceGroup>>(emptyList())
        private set

    fun loadSettings() {
        viewModelScope.launch {
            val lookAndFeel = settingsRepository.getLookAndFeelPageList()
            val settings = settingsRepository.getSettingsPageList()
            val about = settingsRepository.getAboutPageList()
            val autoUpdate = settingsRepository.getAutoUpdatePageList()
            val behavior = settingsRepository.getBehaviorPageList()
            val darkTheme = settingsRepository.getDarkThemePageList()
            val backup = settingsRepository.getBackupPageList()

            settingsPageList = settings
            autoUpdatePageList = autoUpdate
            lookAndFeelPageList = lookAndFeel
            aboutPageList = about
            behaviorPageList = behavior
            darkThemePageList = darkTheme
            backupPageList = backup
        }
    }

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            loadSettings()
            isFirstLaunch = getBoolean(SettingsKeys.FIRST_LAUNCH).first()
        }
    }

    fun onToggle(key: SettingsKeys) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleSettingUseCase(key)
            loadSettings()
        }
    }

    fun setBoolean(key: SettingsKeys, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(key, value)
        }
    }

    fun getBoolean(key: SettingsKeys): Flow<Boolean> = settingsRepository.getBoolean(key)

    fun setInt(key: SettingsKeys, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(key, value)
        }
    }

    fun getInt(key: SettingsKeys): Flow<Int> = settingsRepository.getInt(key)

    fun setFloat(key: SettingsKeys, value: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setFloat(key, value)
        }
    }

    fun getFloat(key: SettingsKeys): Flow<Float> = settingsRepository.getFloat(key)

    fun setString(key: SettingsKeys, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setString(key, value)
        }
    }

    fun getString(key: SettingsKeys): Flow<String> = settingsRepository.getString(key)

    fun onItemClicked(key: SettingsKeys) {
        viewModelScope.launch {
            when (key) {
                SettingsKeys.LOOK_AND_FEEL -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LookAndFeelScreen)
                )

                SettingsKeys.AUTO_UPDATE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AutoUpdateScreen)
                )

                SettingsKeys.BEHAVIOR -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BehaviorScreen)
                )

                SettingsKeys.BACKUP_AND_RESTORE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BackupAndRestoreScreen)
                )

                SettingsKeys.ABOUT -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AboutScreen)
                )

                SettingsKeys.CHANGELOGS -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ChangelogScreen)
                )

                SettingsKeys.CRASH_HISTORY -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.CrashHistoryScreen)
                )

                SettingsKeys.REPORT -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_ISSUE_REPORT)
                )

                SettingsKeys.FEATURE_REQUEST -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_ISSUE_FEATURE_REQUEST)
                )

                SettingsKeys.LICENSE -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_REPO_LICENSE)
                )

                SettingsKeys.GITHUB -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_REPO)
                )

                SettingsKeys.TELEGRAM -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_TELEGRAM_CHANNEL)
                )

                SettingsKeys.LANGUAGE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    _uiEvent.emit(
                        SettingsUiEvent.LaunchIntent(
                            intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    )
                }

                SettingsKeys.DARK_THEME -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.DarkThemeScreen)
                )

                SettingsKeys.RESET_APP_SETTINGS -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsKeys.RESET_APP_SETTINGS)
                )

                SettingsKeys.BACKUP_APP_SETTINGS -> _uiEvent.emit(
                    SettingsUiEvent.RequestDocumentUriForBackup(BackupOption.SETTINGS_ONLY)
                )

                SettingsKeys.BACKUP_APP_DATABASE -> _uiEvent.emit(
                    SettingsUiEvent.RequestDocumentUriForBackup(BackupOption.DATABASE_ONLY)
                )

                SettingsKeys.BACKUP_APP_DATA -> _uiEvent.emit(
                    SettingsUiEvent.RequestDocumentUriForBackup(BackupOption.SETTINGS_AND_DATABASE)
                )

                SettingsKeys.RESTORE_APP_DATA -> _uiEvent.emit(
                    SettingsUiEvent.RequestDocumentUriForRestore
                )

                SettingsKeys.OUTPUT_SAVE_DIRECTORY -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsKeys.OUTPUT_SAVE_DIRECTORY)
                )

                else -> {}
            }
        }
    }

    fun isItemChecked(key: SettingsKeys): Flow<Boolean> {
        return getBoolean(key)
    }

    fun isItemEnabled(key: SettingsKeys): Flow<Boolean> {
        return flowOf(true)
    }

    fun onBooleanItemClicked(key: SettingsKeys) {
        viewModelScope.launch {
            onToggle(key)
        }
    }
}
