package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.autoUpdateGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.AUTO_UPDATE,
        title = R.string.auto_update,
        navigate = {
            navController.navigate(NavRoutes.AutoUpdateScreen) {
                launchSingleTop = true
            }
        },
    ) {
        entry(key = SettingsKeys.AutoUpdate) {
            title(R.string.enable_auto_update)
            description(R.string.des_auto_update)
            icon(R.drawable.ic_auto_update)
        }

        entry(key = SettingsKeys.GithubReleaseType) {
            title(R.string.update_channel)
            description(R.string.des_update_channel)
            keywords(R.string.stable_fdroid, R.string.stable_github, R.string.pre_release_github)
        }

        entry(key = SettingsKeys.EnableDirectDownload) {
            title(R.string.enable_direct_download)
            description(R.string.des_enable_direct_download)
        }
    }
}