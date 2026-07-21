package `in`.hridayan.ashell.settings.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
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
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.settings.data.worker.BackupScheduler
import `in`.hridayan.ashell.settings.domain.model.BackupType
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.domain.usecase.ToggleSettingUseCase
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.provider.SettingsProvider
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey

@Stable
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
            isFirstLaunch = getBoolean(SettingsKeys.FirstLaunch).firstOrNull()
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
    val backupSchedulerPage = SettingsProvider.backupSchedulerPage

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp = _isBackingUp.asStateFlow()


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
                SettingsKeys.LookAndFeel -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LookAndFeelScreen())
                )

                SettingsKeys.AutoUpdate -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AutoUpdateScreen())
                )

                SettingsKeys.Behavior -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BehaviorScreen())
                )

                SettingsKeys.QuickSettingsTiles -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.TileDashboardScreen)
                )

                SettingsKeys.BackupAndRestore -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BackupAndRestoreScreen())
                )

                SettingsKeys.About -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AboutScreen())
                )

                SettingsKeys.Changelogs -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ChangelogScreen)
                )

                SettingsKeys.Translators -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.TranslatorsScreen)
                )

                SettingsKeys.Contributors -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ContributorsScreen)
                )

                SettingsKeys.CrashHistory -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.CrashHistoryScreen)
                )

                SettingsKeys.Report -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_ISSUE_REPORT)
                )

                SettingsKeys.FeatureRequest -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_ISSUE_FEATURE_REQUEST)
                )

                SettingsKeys.Licenses -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LicensesScreen)
                )

                SettingsKeys.Github -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_GITHUB_REPO)
                )

                SettingsKeys.Telegram -> _uiEvent.emit(
                    SettingsUiEvent.OpenUrl(UrlConst.URL_TELEGRAM_CHANNEL)
                )

                SettingsKeys.Language -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.LanguagesScreen)
                )

                SettingsKeys.PaletteStyle -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.PaletteStyle)
                )

                SettingsKeys.DarkTheme -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.DarkThemeScreen())
                )

                SettingsKeys.CustomUiScale -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.UiScaleScreen)
                )

                SettingsKeys.FontFamily -> _uiEvent.emit(
                    SettingsUiEvent.ShowFontStylesBottomSheet
                )

                SettingsKeys.ResetAppSettings -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.ResetSettings)
                )

                // Backup options: show destination dialog if signed-in AND cloud is available
                SettingsKeys.BackupAppSettings -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                SettingsDialogKey.BackupDestination(BackupType.SETTINGS_ONLY)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_ONLY)
                        )
                    }
                }

                SettingsKeys.BackupAppDatabase -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                SettingsDialogKey.BackupDestination(BackupType.DATABASE_ONLY)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.DATABASE_ONLY)
                        )
                    }
                }

                SettingsKeys.BackupAppData -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(
                                SettingsDialogKey.BackupDestination(BackupType.SETTINGS_AND_DATABASE)
                            )
                        )
                    } else {
                        _uiEvent.emit(
                            SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_AND_DATABASE)
                        )
                    }
                }

                SettingsKeys.BackupScheduler -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.BackupSchedulerScreen)
                )

                // Restore: show source dialog if signed in AND cloud is available
                SettingsKeys.RestoreAppData -> {
                    if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                        _uiEvent.emit(
                            SettingsUiEvent.ShowDialog(SettingsDialogKey.RestoreSource)
                        )
                    } else {
                        _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForRestore)
                    }
                }

                SettingsKeys.OutputSaveDirectory -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.ConfigureSaveDir)
                )

                SettingsKeys.AiModelManager -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.AiModelManagerScreen())
                )

                SettingsKeys.AiModels -> _uiEvent.emit(
                    SettingsUiEvent.Navigate(NavRoutes.ModelsScreen)
                )

                SettingsKeys.AiCacheDays -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.AiCacheDays)
                )

                SettingsKeys.AiCacheClear -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.AiCacheClearConfirmation)
                )

                SettingsKeys.AutoBackupTime -> _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(SettingsDialogKey.AutoBackupTimePicker)
                )

                SettingsKeys.AutoBackupFolder -> _uiEvent.emit(
                    SettingsUiEvent.RequestAutoBackupFolderPicker
                )

                else -> {}
            }
        }
    }

    fun rescheduleAutoBackup(
        enabled: Boolean? = null,
        hour: Int? = null,
        minute: Int? = null,
        frequency: Int? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val isEnabled = enabled
                ?: settingsRepository.getBoolean(SettingsKeys.AutoBackupEnabled).firstOrNull()
                ?: false
            Log.i("ABScheduler", "rescheduleAutoBackup() — isEnabled=$isEnabled (explicit=${enabled != null})")
            if (!isEnabled) {
                BackupScheduler.cancel(context)
                return@launch
            }
            val h = hour ?: settingsRepository.getInt(SettingsKeys.AutoBackupTimeHour).firstOrNull() ?: 2
            val m = minute ?: settingsRepository.getInt(SettingsKeys.AutoBackupTimeMinute).firstOrNull() ?: 0
            val f = frequency ?: settingsRepository.getInt(SettingsKeys.AutoBackupFrequency).firstOrNull() ?: 0
            Log.i("ABScheduler", "rescheduleAutoBackup() — h=$h m=$m f=$f")
            BackupScheduler.schedule(context, h, m, f)
        }
    }

    /** Triggers an immediate one-shot backup using the same AutoBackupWorker. */
    fun backupNow() {
        _isBackingUp.value = true
        BackupScheduler.runNow(context)
        // Reset after a delay — WorkManager doesn't give instant completion callbacks easily
        viewModelScope.launch {
            delay(3000.milliseconds)
            _isBackingUp.value = false
        }
    }
}
