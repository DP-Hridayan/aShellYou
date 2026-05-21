package `in`.hridayan.ashell.settings.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.BackupType
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.domain.usecase.ToggleSettingUseCase
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.provider.SettingsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val toggleSettingUseCase: ToggleSettingUseCase,
    private val googleAuthRepository: GoogleAuthRepository,
) : ViewModel() {
    var isFirstLaunch by mutableStateOf<Boolean?>(null)
        private set

    /**
     * Pre-warmed map of all Boolean and Int settings values.
     * Populated in [init] so screens read a ready snapshot via ONE
     * [collectAsState] call instead of N separate DataStore reads.
     */
    private val _settingsState = MutableStateFlow<Map<SettingsKeys, Any?>>(emptyMap())
    val settingsState: StateFlow<Map<SettingsKeys, Any?>> = _settingsState.asStateFlow()

    init {
        viewModelScope.launch {
            isFirstLaunch = getBoolean(SettingsKeys.FIRST_LAUNCH).firstOrNull()
        }
        // Seed immediately with compile-time defaults, then stream live values.
        val boolKeys = SettingsKeys.entries.filter { it.default is Boolean }
        val intKeys  = SettingsKeys.entries.filter { it.default is Int }
        val seed = mutableMapOf<SettingsKeys, Any?>()
        boolKeys.forEach { seed[it] = it.default }
        intKeys.forEach  { seed[it] = it.default }
        _settingsState.value = seed

        boolKeys.forEach { key ->
            getBoolean(key).onEach { v ->
                _settingsState.update { it + (key to v) }
            }.launchIn(viewModelScope)
        }
        intKeys.forEach { key ->
            getInt(key).onEach { v ->
                _settingsState.update { it + (key to v) }
            }.launchIn(viewModelScope)
        }
    }

    val settingsPage = SettingsProvider.settingsPage
    val lookAndFeelPage = SettingsProvider.lookAndFeelPage
    val darkThemePage = SettingsProvider.darkThemePage
    val autoUpdatePage = SettingsProvider.autoUpdatePage
    val behaviorPage = SettingsProvider.behaviorPage
    val aboutPage = SettingsProvider.aboutPage
    val backupPage = SettingsProvider.backupPage

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    fun onToggle(key: SettingsKeys) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleSettingUseCase(key)
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
                    SettingsUiEvent.Navigate(NavRoutes.LookAndFeelScreen())
                )

                SettingsKeys.AUTO_UPDATE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AutoUpdateScreen())
                )

                SettingsKeys.BEHAVIOR -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BehaviorScreen())
                )

                SettingsKeys.QUICK_SETTINGS_TILES -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.TileDashboardScreen)
                )

                SettingsKeys.BACKUP_AND_RESTORE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BackupAndRestoreScreen())
                )

                SettingsKeys.ABOUT -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AboutScreen())
                )

                SettingsKeys.CHANGELOGS -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ChangelogScreen)
                )

                SettingsKeys.TRANSLATORS -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.TranslatorsScreen)
                )

                SettingsKeys.CONTRIBUTORS -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ContributorsScreen)
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

                SettingsKeys.LICENSES -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LicensesScreen)
                )

                SettingsKeys.GITHUB -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_REPO)
                )

                SettingsKeys.TELEGRAM -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_TELEGRAM_CHANNEL)
                )

                SettingsKeys.LANGUAGE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LanguagesScreen)
                )

                SettingsKeys.PALETTE_STYLE -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(DialogKey.Settings.PaletteStyle)
                )

                SettingsKeys.DARK_THEME -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.DarkThemeScreen())
                )

                SettingsKeys.RESET_APP_SETTINGS -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(DialogKey.Settings.ResetSettings)
                )

                // Backup options: show destination dialog if signed-in AND cloud is available
                SettingsKeys.BACKUP_APP_SETTINGS -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                DialogKey.Settings.BackupDestination(BackupType.SETTINGS_ONLY)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_ONLY)
                        )
                    }
                }

                SettingsKeys.BACKUP_APP_DATABASE -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                DialogKey.Settings.BackupDestination(BackupType.DATABASE_ONLY)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.DATABASE_ONLY)
                        )
                    }
                }

                SettingsKeys.BACKUP_APP_DATA -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                DialogKey.Settings.BackupDestination(BackupType.SETTINGS_AND_DATABASE)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_AND_DATABASE)
                        )
                    }
                }

                SettingsKeys.BACKUP_SCHEDULER -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BackupSchedulerScreen)
                )

                // Restore: show source dialog if signed in AND cloud is available
                SettingsKeys.RESTORE_APP_DATA -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(DialogKey.Settings.RestoreSource)
                        )
                    } else {
                        _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForRestore)
                    }
                }

                SettingsKeys.OUTPUT_SAVE_DIRECTORY -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(DialogKey.Settings.ConfigureSaveDir)
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
