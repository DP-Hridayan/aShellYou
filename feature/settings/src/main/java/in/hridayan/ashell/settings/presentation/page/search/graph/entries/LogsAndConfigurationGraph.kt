package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.logsAndConfigurationGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.LOGS_AND_CONFIGURATION,
        title = R.string.logs_and_configuration,
        navigate = {
            navController.navigate(NavRoutes.LogsAndConfigurationScreen) {
                launchSingleTop = true
            }
        },
    ) {
        entry(key = SettingsKeys.CrashHistory) {
            title(R.string.crash_history)
            description(R.string.des_crash_history)
            icon(R.drawable.ic_bug)
        }

        entry(key = SettingsKeys.ExportConfiguration) {
            title(R.string.export_configuration)
            description(R.string.des_export_configuration)
            icon(R.drawable.ic_upload_file)
        }
    }
}
