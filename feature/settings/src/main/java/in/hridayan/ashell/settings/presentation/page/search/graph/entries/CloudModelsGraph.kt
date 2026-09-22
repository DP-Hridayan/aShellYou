package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.cloudModelsGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.CLOUD_MODELS,
        title = R.string.cloud_models,
        navigate = {
            navController.navigate(NavRoutes.CloudModelsScreen) { launchSingleTop = true }
        },
    ) {
        entry(key = SettingsKeys.AiCloudProvider) {
            title(R.string.active_provider)
            description(R.string.des_active_provider)
            icon(R.drawable.ic_cloud_model)
        }
    }
}
