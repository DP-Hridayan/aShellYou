package `in`.hridayan.ashell.settings.presentation.page.backup.viewmodel

import `in`.hridayan.ashell.core.resources.R


import `in`.hridayan.ashell.core.common.SettingsKeys

import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.domain.exception.NoGoogleAccountException
import `in`.hridayan.ashell.core.common.domain.model.BackupType
import `in`.hridayan.ashell.settings.domain.model.DriveAuthEvent
import `in`.hridayan.ashell.settings.domain.model.GoogleUserState
import `in`.hridayan.ashell.settings.domain.model.LastBackupData
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey

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

    private val _currentBackupType = MutableStateFlow(BackupType.SETTINGS_AND_DATABASE)

    val lastLocalBackupTime = settingsRepository.getString(SettingsKeys.LastLocalBackupTime)
    val lastLocalBackupType = settingsRepository.getString(SettingsKeys.LastLocalBackupType)
    val lastCloudBackupTime = settingsRepository.getString(SettingsKeys.LastCloudBackupTime)
    val lastCloudBackupType = settingsRepository.getString(SettingsKeys.LastCloudBackupType)
    val lastAutoLocalSuccessTime = settingsRepository.getString(SettingsKeys.LastAutoBackupLocalSuccessTime)
    val lastAutoCloudSuccessTime = settingsRepository.getString(SettingsKeys.LastAutoBackupCloudSuccessTime)

    private val _localBackupTime = MutableStateFlow<String?>(null)
    val localBackupTime: StateFlow<String?> = _localBackupTime

    private val _localBackupType = MutableStateFlow<String?>(null)
    val localBackupType: StateFlow<String?> = _localBackupType

    val googleUserState: StateFlow<GoogleUserState> = googleAuthRepository.googleUserState

    /** True only in the github flavor — false in fdroid, hiding all Cloud Backup UI. */
    val isCloudBackupAvailable: Boolean = googleAuthRepository.isAvailable

    // Cloud operation loading dialog
    private val _cloudOperationMessage = MutableStateFlow<String?>(null)
    val cloudOperationMessage: StateFlow<String?> = _cloudOperationMessage.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    // Pending cloud restore data (downloaded but not yet applied)
    private val _pendingCloudRestoreBytes = MutableStateFlow<ByteArray?>(null)

    private val _cloudBackupTime = MutableStateFlow<String?>(null)
    val cloudBackupTime: StateFlow<String?> = _cloudBackupTime.asStateFlow()

    private val _cloudBackupType = MutableStateFlow<String?>(null)
    val cloudBackupType: StateFlow<String?> = _cloudBackupType.asStateFlow()

    private val _isFetchingCloudBackup = MutableStateFlow(false)
    val isFetchingCloudBackup: StateFlow<Boolean> = _isFetchingCloudBackup.asStateFlow()

    private val _showCloudRestoreConfirm = MutableStateFlow(false)
    val showCloudRestoreConfirm: StateFlow<Boolean> = _showCloudRestoreConfirm.asStateFlow()

    // Consent flow: emits IntentSender when Drive scope consent is needed
    private val _consentIntentSender = MutableSharedFlow<IntentSender>()
    val consentIntentSender = _consentIntentSender.asSharedFlow()

    val lastBackupData: StateFlow<LastBackupData> =
        combine(
            listOf(
                lastLocalBackupTime,
                lastLocalBackupType,
                lastCloudBackupTime,
                lastCloudBackupType,
                googleUserState,
                isFetchingCloudBackup,
                lastAutoLocalSuccessTime,
                lastAutoCloudSuccessTime,
            )
        ) { values ->

            val manualLocalTimeRaw = values[0] as String
            val localTypeRaw = values[1] as String
            val manualCloudTimeRaw = values[2] as String
            val cloudTypeRaw = values[3] as String
            val userState = values[4] as GoogleUserState
            val isFetching = values[5] as Boolean
            val autoLocalTimeRaw = values[6] as String
            val autoCloudTimeRaw = values[7] as String

            val (localTimeRaw, localIsAuto) = pickLatest(manualLocalTimeRaw, autoLocalTimeRaw)
            val (cloudTimeRaw, cloudIsAuto) = pickLatest(manualCloudTimeRaw, autoCloudTimeRaw)

            LastBackupData(
                localTime = formatLocalBackupTime(localTimeRaw),
                localType = localTypeRaw.ifEmpty {
                    context.getString(R.string.none)
                },
                localIsAuto = localIsAuto,
                cloudTime = formatCloudBackupTime(cloudTimeRaw, isFetching),
                cloudType = formatCloudBackupType(
                    cloudTypeRaw,
                    userState.isSignedIn,
                    isFetching
                ),
                cloudIsAuto = cloudIsAuto,
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                LastBackupData(
                    localTime = "",
                    localType = context.getString(R.string.none),
                    cloudTime = "",
                    cloudType = context.getString(R.string.not_signed_in)
                )
            )

    // Tracks what operation to retry after consent is granted
    private sealed class PendingOperation {
        data class Backup(val type: BackupType) : PendingOperation()
        data object Restore : PendingOperation()
    }

    private val _pendingOperation = MutableStateFlow<PendingOperation?>(null)

    init {
        // Forward consent requests from Drive repo to UI
        viewModelScope.launch {
            googleDriveRepository.authEvents.collect { authEvent ->
                when (authEvent) {
                    is DriveAuthEvent.ConsentRequired -> {
                        Log.d(TAG, "consent required — forwarding to UI")
                        _consentIntentSender.emit(authEvent.intentSender)
                    }
                }
            }
        }
    }

    fun initiateBackup(type: BackupType) {
        _currentBackupType.value = type
    }

    fun performLocalBackup(uri: Uri) {
        viewModelScope.launch {
            val success = backupAndRestoreRepository.backupToDevice(uri, _currentBackupType.value)

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
            _localBackupTime.value = time

            val type = backupAndRestoreRepository.getBackupTypeFromFile(uri)
            _localBackupType.value = type
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "signInWithGoogle: starting...")
            _isSigningIn.value = true

            val result = googleAuthRepository.signIn(context)

            _isSigningIn.value = false

            result.fold(
                onSuccess = { email ->
                    Log.d(TAG, "signInWithGoogle: success — email=$email")
                    _uiEvent.emit(
                        SettingsUiEvent.ShowToast(
                            context.getString(R.string.signed_in_as, email)
                        )
                    )

                    // Immediately request Drive scope so consent appears right after sign-in
                    Log.d(TAG, "signInWithGoogle: pre-authorizing Drive scope...")
                    val authorized = googleDriveRepository.ensureAuthorized()

                    if (authorized) {
                        // This is done to show correct last backup time per Google account
                        syncCloudBackupDetailsOnSignIn()
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "signInWithGoogle: failed — ${error.message}")
                    if (error is NoGoogleAccountException) {
                        viewModelScope.launch {
                            _uiEvent.emit(SettingsUiEvent.ShowDialog(SettingsDialogKey.NoGoogleAccount))
                        }
                    } else {
                        viewModelScope.launch {
                            val errorMessage =
                                error.message ?: context.getString(R.string.unknown_error)
                            _uiEvent.emit(
                                SettingsUiEvent.ShowToast(
                                    context.getString(R.string.sign_in_failed) + ": $errorMessage"
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthRepository.signOut()
            _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.signed_out)))
            settingsRepository.setString(SettingsKeys.LastCloudBackupTime, "")
        }
    }

    /** Called by the Screen after user grants Drive scope consent. Retries pending operation. */
    fun onConsentGranted() {
        Log.d(TAG, "onConsentGranted: retrying pending operation=${_pendingOperation.value}")
        googleDriveRepository.onConsentGranted()

        when (val op = _pendingOperation.value) {
            is PendingOperation.Backup -> {
                _pendingOperation.value = null
                backupToGoogleDrive(op.type)
            }

            is PendingOperation.Restore -> {
                _pendingOperation.value = null
                downloadFromGoogleDrive()
            }

            null -> {
                Log.d(TAG, "onConsentGranted: no pending operation to retry")
                syncCloudBackupDetailsOnSignIn()
            }
        }
    }

    fun onConsentDenied() {
        Log.d(TAG, "onConsentDenied")
        _pendingOperation.value = null
        _cloudOperationMessage.value = null
        viewModelScope.launch {
            _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.sign_in_failed)))
        }
    }

    fun backupToGoogleDrive(type: BackupType) {
        viewModelScope.launch {
            Log.d(TAG, "backupToGoogleDrive: option=$type")
            _pendingOperation.value = PendingOperation.Backup(type)
            _cloudOperationMessage.value = context.getString(R.string.uploading_backup)

            val bytes = backupAndRestoreRepository.generateCloudBackupBytes(type)
            if (bytes == null) {
                Log.e(TAG, "backupToGoogleDrive: generateBackupBytes returned null")
                _cloudOperationMessage.value = null
                _pendingOperation.value = null
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_failed)))
                return@launch
            }

            Log.d(TAG, "backupToGoogleDrive: generated ${bytes.size} bytes, uploading...")
            val fileName = "backup_${System.currentTimeMillis()}.ashellyou"
            val success = googleDriveRepository.uploadBackup(bytes, fileName)

            // If consent was needed, the consent flow will retry this operation
            if (!success && googleDriveRepository.isConsentPending) {
                Log.d(TAG, "backupToGoogleDrive: consent needed — waiting for user")
                return@launch
            }

            _cloudOperationMessage.value = null
            _pendingOperation.value = null

            if (success) {
                Log.d(TAG, "backupToGoogleDrive: upload SUCCESS")
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                val backupTime = LocalDateTime.now().format(formatter)
                settingsRepository.setString(SettingsKeys.LastCloudBackupTime, backupTime)
                settingsRepository.setString(SettingsKeys.LastCloudBackupType, type.name)
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_successful)))
            } else {
                Log.e(TAG, "backupToGoogleDrive: upload FAILED")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_backup_failed)))
            }
        }
    }

    /** Step 1: Download backup from Drive and show confirmation dialog */
    fun downloadFromGoogleDrive() {
        viewModelScope.launch {
            Log.d(TAG, "downloadFromGoogleDrive: starting...")
            _pendingOperation.value = PendingOperation.Restore
            _cloudOperationMessage.value = context.getString(R.string.downloading_backup)

            val result = googleDriveRepository.downloadBackup()

            // If consent was needed, the consent flow will retry this operation
            if (result == null && googleDriveRepository.isConsentPending) {
                Log.d(TAG, "downloadFromGoogleDrive: consent needed — waiting for user")
                _cloudOperationMessage.value = null
                return@launch
            }

            _cloudOperationMessage.value = null
            _pendingOperation.value = null

            if (result == null) {
                Log.e(TAG, "downloadFromGoogleDrive: no backup found on Drive")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.no_cloud_backup_found)))
                return@launch
            }

            val (bytes, fileName) = result
            Log.d(TAG, "downloadFromGoogleDrive: downloaded ${bytes.size} bytes (file=$fileName)")

            // Store pending bytes and extract backup time
            _pendingCloudRestoreBytes.value = bytes

            val time = backupAndRestoreRepository.getBackupTimeFromBytes(bytes)
            _cloudBackupTime.value = time

            val type = backupAndRestoreRepository.getBackupTypeFromBytes(bytes)
            _cloudBackupType.value = type

            _showCloudRestoreConfirm.value = true
        }
    }

    private fun syncCloudBackupDetailsOnSignIn() {
        viewModelScope.launch {
            Log.d(TAG, "syncCloudBackupTimeSilently: starting...")

            _isFetchingCloudBackup.value = true

            val result = googleDriveRepository.downloadBackup()

            if (result == null) {
                if (googleDriveRepository.isConsentPending) {
                    Log.d(TAG, "syncCloudBackupTimeSilently: consent required, skipping for now")
                    _isFetchingCloudBackup.value = false
                    return@launch
                }

                Log.d(TAG, "syncCloudBackupTimeSilently: no backup found")
                settingsRepository.setString(SettingsKeys.LastCloudBackupTime, "")
                settingsRepository.setString(SettingsKeys.LastCloudBackupType, "")
                _isFetchingCloudBackup.value = false
                return@launch
            }

            val (bytes, _) = result

            val time = backupAndRestoreRepository.getBackupTimeFromBytes(bytes)

            if (time != null) {
                Log.d(TAG, "syncCloudBackupTimeSilently: extracted time=$time")

                settingsRepository.setString(
                    SettingsKeys.LastCloudBackupTime,
                    time
                )
            } else {
                Log.e(TAG, "syncCloudBackupTimeSilently: failed to extract time")
            }

            val type = backupAndRestoreRepository.getBackupTypeFromBytes(bytes)

            if (type != null) {
                Log.d(TAG, "syncCloudBackupTypeSilently: extracted type=$type")

                settingsRepository.setString(
                    SettingsKeys.LastCloudBackupType,
                    type
                )
            } else {
                Log.e(TAG, "syncCloudBackupTypeSilently: failed to extract type")
            }

            _isFetchingCloudBackup.value = false
        }
    }

    /** Step 2: User confirmed — apply the downloaded restore */
    fun confirmCloudRestore() {
        viewModelScope.launch {
            _showCloudRestoreConfirm.value = false
            val bytes = _pendingCloudRestoreBytes.value
            if (bytes == null) {
                Log.e(TAG, "confirmCloudRestore: no pending bytes!")
                _uiEvent.emit(SettingsUiEvent.ShowToast(context.getString(R.string.cloud_restore_failed)))
                return@launch
            }

            _cloudOperationMessage.value = context.getString(R.string.restoring_backup)
            Log.d(TAG, "confirmCloudRestore: restoring ${bytes.size} bytes...")
            val success = backupAndRestoreRepository.restoreFromBytes(bytes)

            _cloudOperationMessage.value = null
            _pendingCloudRestoreBytes.value = null

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
        _pendingCloudRestoreBytes.value = null
        _cloudBackupTime.value = null
        _cloudBackupType.value = null
    }


    private fun formatLocalBackupTime(raw: String?): String {
        if (raw.isNullOrEmpty()) return ""

        return raw.split(" ").let { parts ->
            val rawDate = parts.getOrNull(0).orEmpty()
            val time = parts.getOrNull(1).orEmpty()

            val formattedDate = try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val outputFormatter = DateTimeFormatter.ofPattern("d MMMM, yyyy")

                val parsedDate = LocalDate.parse(rawDate, inputFormatter)
                parsedDate.format(outputFormatter)
            } catch (e: Exception) {
                rawDate
            }

            "$formattedDate | $time"
        }
    }

    private fun formatCloudBackupTime(
        rawTime: String?,
        isFetching: Boolean
    ): String {
        return when {
            isFetching -> context.getString(R.string.fetching_backup_time)

            !rawTime.isNullOrEmpty() -> rawTime.split(" ").let { parts ->
                val rawDate = parts.getOrNull(0).orEmpty()
                val time = parts.getOrNull(1).orEmpty()

                val formattedDate = try {
                    val inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val outputFormatter = DateTimeFormatter.ofPattern("d MMMM, yyyy")

                    val parsedDate = LocalDate.parse(rawDate, inputFormatter)
                    parsedDate.format(outputFormatter)
                } catch (e: Exception) {
                    rawDate
                }

                "$formattedDate | $time"
            }

            else -> ""
        }
    }

    private fun formatCloudBackupType(
        rawType: String?,
        isSignedIn: Boolean,
        isFetching: Boolean
    ): String {
        return when {
            !isSignedIn -> context.getString(R.string.not_signed_in)
            isFetching -> context.getString(R.string.fetching_backup_type)
            !rawType.isNullOrEmpty() -> rawType
            else -> context.getString(R.string.none)
        }
    }

    /** Returns the more recent of two "dd-MM-yyyy HH:mm" timestamps and whether the auto one won. */
    private fun pickLatest(manual: String, auto: String): Pair<String, Boolean> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val manualDt = runCatching { LocalDateTime.parse(manual.trim(), formatter) }.getOrNull()
        val autoDt = runCatching { LocalDateTime.parse(auto.trim(), formatter) }.getOrNull()

        return when {
            manualDt == null && autoDt == null -> Pair("", false)
            manualDt == null -> Pair(auto, true)
            autoDt == null -> Pair(manual, false)
            autoDt.isAfter(manualDt) -> Pair(auto, true)
            else -> Pair(manual, false)
        }
    }
}
