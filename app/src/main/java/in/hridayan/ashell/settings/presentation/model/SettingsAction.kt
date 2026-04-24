package `in`.hridayan.ashell.settings.presentation.model

import android.content.Intent
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.domain.model.BackupType

sealed interface SettingsAction {
    data class Navigate(val route: NavRoutes) : SettingsAction
    data class OpenUrl(val url: String) : SettingsAction
    data class ShowDialog(val key: DialogKey) : SettingsAction
    data class LaunchIntent(val intent: Intent) : SettingsAction
    data class RequestBackup(val type: BackupType) : SettingsAction
    data object RequestRestore : SettingsAction
    data class Custom(val action: suspend () -> Unit) : SettingsAction
    data object OpenLanguageSettings : SettingsAction
}
