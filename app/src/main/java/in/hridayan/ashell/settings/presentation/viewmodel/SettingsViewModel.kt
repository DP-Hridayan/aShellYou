package `in`.hridayan.ashell.settings.presentation.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
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
     * Raw DataStore preferences — collected once per screen via [rememberController].
     * Replaces the old pre-warming pattern (21 coroutines at startup).
     */
    val preferences: StateFlow<Preferences> = settingsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyPreferences())

    init {
        viewModelScope.launch {
            isFirstLaunch = getBoolean(SettingsKeys.FIRST_LAUNCH).firstOrNull()
        }
    }

    val settingsPage = SettingsProvider.settingsPage
    val lookAndFeelPage = SettingsProvider.lookAndFeelPage
    val darkThemePage = SettingsProvider.darkThemePage
    val autoUpdatePage = SettingsProvider.autoUpdatePage
    val behaviorPage = SettingsProvider.behaviorPage
    val aboutPage = SettingsProvider.aboutPage
    val backupPage = SettingsProvider.backupPage
    val aiModelsPage = SettingsProvider.aiModelsPage

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    fun onToggle(key: SettingsKeys<Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleSettingUseCase(key)
        }
    }

    fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBoolean(key, value)
        }
    }

    fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean> = settingsRepository.getBoolean(key)

    fun setInt(key: SettingsKeys<Int>, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setInt(key, value)
        }
    }

    fun getInt(key: SettingsKeys<Int>): Flow<Int> = settingsRepository.getInt(key)

    fun setFloat(key: SettingsKeys<Float>, value: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setFloat(key, value)
        }
    }

    fun getFloat(key: SettingsKeys<Float>): Flow<Float> = settingsRepository.getFloat(key)

    fun setString(key: SettingsKeys<String>, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setString(key, value)
        }
    }

    fun getString(key: SettingsKeys<String>): Flow<String> = settingsRepository.getString(key)

    fun onItemClicked(key: SettingsKeys<*>) {
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

                SettingsKeys.UI_SCALE -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.UiScaleScreen)
                )

                SettingsKeys.FONT_FAMILY -> _uiEvent.emit(
                    SettingsUiEvent.ShowFontStylesBottomSheet
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

                SettingsKeys.AI_MODEL_MANAGER -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AiModelManagerScreen())
                )

                SettingsKeys.AI_MODELS -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ModelsScreen)
                )

                SettingsKeys.AI_CACHE_DAYS -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(DialogKey.Settings.AiCacheDays)
                )

                SettingsKeys.AI_CACHE_CLEAR -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(DialogKey.Settings.AiCacheClearConfirmation)
                )

                else -> {}
            }
        }
    }
}
