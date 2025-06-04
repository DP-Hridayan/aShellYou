package `in`.hridayan.ashell.settings.presentation.page.backup.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.domain.model.BackupOption
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupAndRestoreViewModel @Inject constructor(
    private val backupAndRestoreRepository: BackupAndRestoreRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentBackupOption: BackupOption = BackupOption.SETTINGS_AND_DATABASE

    private val _backupTime = MutableStateFlow<String?>(null)
    val backupTime: StateFlow<String?> = _backupTime

    fun initiateBackup(option: BackupOption) {
        currentBackupOption = option
    }

    fun performBackup(uri: Uri) {
        viewModelScope.launch {
            val success = backupAndRestoreRepository.backupDataToFile(uri, currentBackupOption)

            val message =
                if (success) context.getString(R.string.backup_successful) else context.getString(R.string.backup_failed)
            _uiEvent.emit(SettingsUiEvent.ShowToast(message))
        }
    }

    fun performRestore(uri: Uri) {
        viewModelScope.launch {
            val success = backupAndRestoreRepository.restoreDataFromFile(uri)

            val message =
                if (success) context.getString(R.string.restore_successful) else context.getString(R.string.restore_failed)
            _uiEvent.emit(SettingsUiEvent.ShowToast(message))
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            val success = settingsRepository.resetAndRestoreDefaults()

            val message =
                if (success) context.getString(R.string.reset_settings_successful) else context.getString(
                    R.string.reset_settings_failed
                )

            _uiEvent.emit(SettingsUiEvent.ShowToast(message))
        }
    }

    fun loadBackupTime(uri: Uri) {
        viewModelScope.launch {
            val time = backupAndRestoreRepository.getBackupTimeFromFile(uri)
            _backupTime.value = time
        }
    }
}