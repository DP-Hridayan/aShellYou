package `in`.hridayan.ashell.settings.presentation.components.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.common.domain.model.BackupType

sealed interface SettingsDialogKey : DialogKey {
    object ConfigureSaveDir : SettingsDialogKey
    object LatestVersion : SettingsDialogKey
    object ResetSettings : SettingsDialogKey
    object RestoreBackup : SettingsDialogKey
    data class BackupDestination(val backupType: BackupType) : SettingsDialogKey {
        override fun matches(other: DialogKey?): Boolean = other is BackupDestination
    }
    object RestoreSource : SettingsDialogKey
    object ConfirmGoogleSignOut : SettingsDialogKey
    object NoGoogleAccount : SettingsDialogKey
    object PaletteStyle : SettingsDialogKey
    object AiCacheDays : SettingsDialogKey
    object AiCacheClearConfirmation : SettingsDialogKey
    object AutoBackupTimePicker : SettingsDialogKey
}
