package `in`.hridayan.ashell.settings.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.backup.BackupType
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.settings.data.worker.BackupScheduler
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.usecase.ToggleSettingUseCase
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val toggleSettingUseCase: ToggleSettingUseCase,
    private val googleAuthRepository: GoogleAuthRepository,
) : ViewModel() {
    var isFirstLaunch by mutableStateOf<Boolean?>(null)
        private set

    var defaultLaunchIsLocalAdb by mutableStateOf<Boolean?>(null)
        private set

    /**
     * Raw DataStore preferences.
     * Replaces the old pre-warming pattern (21 coroutines at startup).
     */
    val preferences: StateFlow<Preferences> = settingsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyPreferences())

    /**
     * An arbitrary trigger that updates whenever [preferences] changes.
     * Useful for Compose UI that needs to observe changes without importing DataStore types.
     */
    private var prefsCounter = 0
    val prefsUpdateTrigger: StateFlow<Int> = preferences
        .map { ++prefsCounter }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        viewModelScope.launch {
            isFirstLaunch = getBoolean(SettingsKeys.FirstLaunch).firstOrNull()
            defaultLaunchIsLocalAdb =
                getBoolean(SettingsKeys.DefaultLaunchIsLocalAdb).firstOrNull()
        }
    }

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

    /**
     * Returns the current boolean value for [key] read synchronously from the cached
     * [preferences] [StateFlow]. Falls back to the key's default if not yet stored.
     *
     * @param key A [SettingsKeys] whose [SettingsKeys.default] is a [Boolean].
     * @return The stored value, or `false` if the key has no boolean default.
     */
    fun currentBoolean(key: Any): Boolean {
        val sk = key as? SettingsKeys<*> ?: return false
        if (sk.default !is Boolean) return false
        return preferences.value[androidx.datastore.preferences.core.booleanPreferencesKey(sk.name)]
            ?: (sk.default as Boolean)
    }

    /**
     * Returns the current int value for [key] read synchronously from the cached
     * [preferences] [StateFlow]. Falls back to the key's default if not yet stored.
     *
     * @param key A [SettingsKeys] whose [SettingsKeys.default] is an [Int].
     * @return The stored value, or `-1` if the key has no int default.
     */
    fun currentInt(key: Any): Int {
        val sk = key as? SettingsKeys<*> ?: return -1
        if (sk.default !is Int) return -1
        return preferences.value[androidx.datastore.preferences.core.intPreferencesKey(sk.name)]
            ?: (sk.default as Int)
    }

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

    fun handleBackupSettingsClick() {
        viewModelScope.launch {
            if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(
                        SettingsDialogKey.BackupDestination(
                            BackupType.SETTINGS_ONLY
                        )
                    )
                )
            } else {
                _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_ONLY))
            }
        }
    }

    fun handleBackupDatabaseClick() {
        viewModelScope.launch {
            if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(
                        SettingsDialogKey.BackupDestination(
                            BackupType.DATABASE_ONLY
                        )
                    )
                )
            } else {
                _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForBackup(BackupType.DATABASE_ONLY))
            }
        }
    }

    fun handleBackupAllClick() {
        viewModelScope.launch {
            if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                _uiEvent.emit(
                    SettingsUiEvent.ShowDialog(
                        SettingsDialogKey.BackupDestination(
                            BackupType.SETTINGS_AND_DATABASE
                        )
                    )
                )
            } else {
                _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForBackup(BackupType.SETTINGS_AND_DATABASE))
            }
        }
    }

    fun handleRestoreClick() {
        viewModelScope.launch {
            if (googleAuthRepository.isAvailable && googleAuthRepository.googleUserState.value.isSignedIn) {
                _uiEvent.emit(SettingsUiEvent.ShowDialog(SettingsDialogKey.RestoreSource))
            } else {
                _uiEvent.emit(SettingsUiEvent.RequestDocumentUriForRestore)
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
            Log.i(
                "ABScheduler",
                "rescheduleAutoBackup() — isEnabled=$isEnabled (explicit=${enabled != null})"
            )
            if (!isEnabled) {
                BackupScheduler.cancel(context)
                return@launch
            }
            val h = hour ?: settingsRepository.getInt(SettingsKeys.AutoBackupTimeHour).firstOrNull()
            ?: 2
            val m =
                minute ?: settingsRepository.getInt(SettingsKeys.AutoBackupTimeMinute).firstOrNull()
                ?: 0
            val f = frequency ?: settingsRepository.getInt(SettingsKeys.AutoBackupFrequency)
                .firstOrNull() ?: 0
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

