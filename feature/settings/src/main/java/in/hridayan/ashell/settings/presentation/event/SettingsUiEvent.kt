package `in`.hridayan.ashell.settings.presentation.event

import android.content.Intent
import `in`.hridayan.ashell.core.common.domain.model.BackupType
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed class SettingsUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent()
    data class Navigate(val route: Any) : SettingsUiEvent()
    data class ShowDialog(val key: DialogKey) : SettingsUiEvent()
    data class OpenUrl(val url: String) : SettingsUiEvent()
    data class LaunchIntent(val intent: Intent) : SettingsUiEvent()
    data class RequestPermission(val permission: String) : SettingsUiEvent()

    data class RequestDocumentUriForBackup(val backupType: BackupType) : SettingsUiEvent()
    object RequestDocumentUriForRestore : SettingsUiEvent()

    // Google Drive events
    object RequestGoogleSignIn : SettingsUiEvent()
    data class RequestGoogleDriveBackup(val backupType: BackupType) : SettingsUiEvent()
    object RequestGoogleDriveRestore : SettingsUiEvent()
    object ShowFontStylesBottomSheet : SettingsUiEvent()
    object RequestAutoBackupFolderPicker : SettingsUiEvent()
}
