package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.backupAndRestoreGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.BACKUP_AND_RESTORE,
        title = R.string.backup_and_restore,
        navigate = {
            navController.navigate(NavRoutes.BackupAndRestoreScreen) {
                launchSingleTop = true
            }
        },
    ) {
        entry(key = SettingsKeys.BackupAppSettings) {
            title(R.string.backup_settings)
            description(R.string.des_backup_settings)
            icon(R.drawable.ic_handyman)
        }

        entry(key = SettingsKeys.BackupAppDatabase) {
            title(R.string.backup_app_database)
            description(R.string.des_backup_app_database)
            icon(R.drawable.ic_database)
        }

        entry(key = SettingsKeys.BackupAppData) {
            title(R.string.backup_all_data)
            description(R.string.des_backup_all_data)
            icon(R.drawable.ic_upload_file)
        }

        entry(key = SettingsKeys.BackupScheduler) {
            title(R.string.backup_scheduler)
            description(R.string.des_backup_scheduler)
            icon(R.drawable.ic_schedule)
        }

        entry(key = SettingsKeys.RestoreAppData) {
            title(R.string.restore_app_data)
            description(R.string.des_restore_app_data)
            icon(R.drawable.ic_restore_page)
        }

        entry(key = SettingsKeys.ResetAppSettings) {
            title(R.string.reset_app_settings)
            description(R.string.des_reset_app_settings)
            icon(R.drawable.ic_reset_settings)
        }

        backupSchedulerGraph(navController)
    }
}