package `in`.hridayan.ashell.settings.presentation.page.backup.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.BackupOption
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupAndRestoreViewModel @Inject constructor(
    private val backupAndRestoreRepository: BackupAndRestoreRepository,
    private val settingsRepository: SettingsRepository,
    private val googleAuthRepository: GoogleAuthRepository,
    private val googleDriveRepository: GoogleDriveRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "BackupRestoreVM"
    }

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentBackupOption: BackupOption = BackupOption.SETTINGS_AND_DATABASE

    private val _backupTime = MutableStateFlow<String?>(null)
    val backupTime: StateFlow<String?> = _backupTime

    // Google Sign-In state
    val isSignedIn: StateFlow<Boolean> = googleAuthRepository.isSignedIn
    val userEmail: StateFlow<String?> = googleAuthRepository.userEmail
    val userName: StateFlow<String?> = googleAuthRepository.userName
    val userPhotoUrl = googleAuthRepository.userPhotoUrl

    // Cloud operation loading dialog
    private val _cloudOperationMessage = MutableStateFlow<String?>(null)
    val cloudOperationMessage: StateFlow<String?> = _cloudOperationMessage.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    // Pending cloud restore data (downloaded but not yet applied)
    private var pendingCloudRestoreBytes: ByteArray? = null

    private val _cloudBackupTime = MutableStateFlow<String?>(null)
    val cloudBackupTime: StateFlow<String?> = _cloudBackupTime.asStateFlow()

    // Flag indicating cloud restore confirm dialog should show
    private val _showCloudRestoreConfirm = MutableStateFlow(false)
    val showCloudRestoreConfirm: StateFlow<Boolean> = _showCloudRestoreConfirm.asStateFlow()

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

    // --- Google Sign-In (Credential Manager — modern bottom sheet) ---

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "signInWithGoogle: starting...")
            _isSigningIn.value = true

            val result = googleAuthRepository.signIn(context)

            _isSigningIn.value = false

            result.fold(
                onSuccess = { email ->
                    Log.d(TAG, "signInWithGoogle: success — email=$email")
                    _uiEvent.emit(SettingsUiEvent.ShowToast(
                        context.getString(R.string.signed_in_as, email)
                    ))
                },
                onFailure = { error ->
                    Log.e(TAG, "signInWithGoogle: failed — ${error.message}")
                    _uiEvent.emit(SettingsUiEvent.ShowToast(
                        context.getString(R.string.sign_in_failed)
                    ))
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthRepository.signOut()
            _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.signed_out)))
        }
    }

    // --- Google Drive Backup ---

    fun backupToGoogleDrive(option: BackupOption) {
        viewModelScope.launch {
            Log.d(TAG, "backupToGoogleDrive: option=$option")
            _cloudOperationMessage.value = context.getString(R.string.uploading_backup)

            val bytes = backupAndRestoreRepository.generateBackupBytes(option)
            if (bytes == null) {
                Log.e(TAG, "backupToGoogleDrive: generateBackupBytes returned null")
                _cloudOperationMessage.value = null
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_failed)))
                return@launch
            }

            Log.d(TAG, "backupToGoogleDrive: generated ${bytes.size} bytes, uploading...")
            val fileName = "backup_${System.currentTimeMillis()}.ashellyou"
            val success = googleDriveRepository.uploadBackup(bytes, fileName)

            _cloudOperationMessage.value = null

            if (success) {
                Log.d(TAG, "backupToGoogleDrive: upload SUCCESS")
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                val backupTime = java.time.LocalDateTime.now().format(formatter)
                settingsRepository.setString(SettingsKeys.LAST_CLOUD_BACKUP_TIME, backupTime)
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_successful)))
            } else {
                Log.e(TAG, "backupToGoogleDrive: upload FAILED")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_failed)))
            }
        }
    }

    // --- Google Drive Restore (two-step: download → confirm → apply) ---

    /** Step 1: Download backup from Drive and show confirmation dialog */
    fun downloadFromGoogleDrive() {
        viewModelScope.launch {
            Log.d(TAG, "downloadFromGoogleDrive: starting...")
            _cloudOperationMessage.value = context.getString(R.string.downloading_backup)

            val result = googleDriveRepository.downloadBackup()
            _cloudOperationMessage.value = null

            if (result == null) {
                Log.e(TAG, "downloadFromGoogleDrive: no backup found on Drive")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.no_cloud_backup_found)))
                return@launch
            }

            val (bytes, fileName) = result
            Log.d(TAG, "downloadFromGoogleDrive: downloaded ${bytes.size} bytes (file=$fileName)")

            // Store pending bytes and extract backup time
            pendingCloudRestoreBytes = bytes
            val time = backupAndRestoreRepository.getBackupTimeFromBytes(bytes)
            _cloudBackupTime.value = time
            _showCloudRestoreConfirm.value = true
        }
    }

    /** Step 2: User confirmed — apply the downloaded restore */
    fun confirmCloudRestore() {
        viewModelScope.launch {
            _showCloudRestoreConfirm.value = false
            val bytes = pendingCloudRestoreBytes
            if (bytes == null) {
                Log.e(TAG, "confirmCloudRestore: no pending bytes!")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_restore_failed)))
                return@launch
            }

            _cloudOperationMessage.value = context.getString(R.string.restoring_backup)
            Log.d(TAG, "confirmCloudRestore: restoring ${bytes.size} bytes...")
            val success = backupAndRestoreRepository.restoreFromBytes(bytes)

            _cloudOperationMessage.value = null
            pendingCloudRestoreBytes = null

            val message = if (success) {
                Log.d(TAG, "confirmCloudRestore: SUCCESS")
                context.getString(R.string.cloud_restore_successful)
            } else {
                Log.e(TAG, "confirmCloudRestore: FAILED")
                context.getString(R.string.cloud_restore_failed)
            }
            _uiEvent.emit(SettingsUiEvent.ShowToast(message))
        }
    }

    /** User cancelled cloud restore */
    fun cancelCloudRestore() {
        _showCloudRestoreConfirm.value = false
        pendingCloudRestoreBytes = null
        _cloudBackupTime.value = null
    }
}