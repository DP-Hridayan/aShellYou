package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.backupSchedulerGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.BACKUP_SCHEDULER,
        title = R.string.backup_scheduler,
        navigate = {
            navController.navigate(NavRoutes.BackupSchedulerScreen) {
                launchSingleTop = true
            }
        },
    ) {
        entry(key = SettingsKeys.AutoBackupEnabled) {
            title(R.string.enable_auto_backup)
        }

        entry(key = SettingsKeys.AutoBackupTime) {
            title(R.string.backup_time)
            description(R.string.des_auto_backup_time)
            icon(R.drawable.ic_schedule)
        }

        entry(key = SettingsKeys.AutoBackupFrequency) {
            title(R.string.frequency)
            keywords(R.string.daily, R.string.weekly, R.string.monthly)
        }

        entry(key = SettingsKeys.AutoBackupType) {
            title(R.string.auto_backup_content_type)
            keywords(R.string.all_data, R.string.settings_only, R.string.databases_only)
        }

        entry(key = SettingsKeys.AutoBackupFolder) {
            title(R.string.auto_backup_folder)
            description(R.string.des_auto_backup_folder)
            icon(R.drawable.ic_directory)
        }

        entry(key = SettingsKeys.AutoBackupDeleteExisting) {
            title(R.string.auto_delete_existing_backups)
            description(R.string.des_auto_delete_existing_backups)
            icon(R.drawable.ic_auto_delete)
        }
    }
}