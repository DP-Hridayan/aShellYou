package `in`.hridayan.ashell.settings.presentation.event

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.settings.domain.model.BackupOption

sealed class SettingsUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent()
    data class Navigate(val route: NavKey) : SettingsUiEvent()
    data class ShowDialog(val key : DialogKey) : SettingsUiEvent()
    data class OpenUrl(val url:String) : SettingsUiEvent()
    data class LaunchIntent(val intent: Intent) : SettingsUiEvent()
    data class RequestPermission(val permission: String) : SettingsUiEvent()

    data class RequestDocumentUriForBackup(val backupOption: BackupOption) : SettingsUiEvent()
    object RequestDocumentUriForRestore : SettingsUiEvent()
}
