package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.aiModelsGraph(
    navController: NavController,
    aiEnabled: Boolean,
    aiCacheEnabled: Boolean
) {
    screen(
        id = SettingsGraphId.AI_MODELS,
        title = R.string.ai_models,
        navigate = { navController.navigate(NavRoutes.AiModelsScreen) { launchSingleTop = true } },
    ) {
        availableWhen(aiEnabled)

        entry(key = SettingsKeys.CloudModels) {
            title(R.string.cloud_models)
            description(R.string.des_cloud_models)
            icon(R.drawable.ic_cloud_model)
        }

        entry(key = SettingsKeys.AiSkillCommandExecution) {
            title(R.string.command_execution)
            description(R.string.des_command_execution)
            icon(R.drawable.ic_terminal)
        }

        entry(key = SettingsKeys.AiSkillQuickSettings) {
            title(R.string.quick_settings_tiles)
            description(R.string.des_quick_settings_tiles)
            icon(R.drawable.ic_dashboard)
        }

        entry(key = SettingsKeys.AiSkillPackages) {
            title(R.string.packages)
            description(R.string.des_packages)
            icon(R.drawable.ic_package)
        }

        entry(key = SettingsKeys.AiSkillDatabase) {
            title(R.string.database_modification)
            description(R.string.des_database_modification)
            icon(R.drawable.ic_database)
        }

        entry(key = SettingsKeys.AiSkillDeviceDiagnostics) {
            title(R.string.device_diagnostics)
            description(R.string.des_device_diagnostics)
            icon(R.drawable.ic_troubleshoot)
        }

        entry(key = SettingsKeys.AiCacheEnabled) {
            title(R.string.ai_cache_enabled)
            description(R.string.des_ai_cache_enabled)
            icon(Icons.Rounded.Cached)
        }

        entry(key = SettingsKeys.AiCacheAutoClear) {
            title(R.string.auto_clear_cache)
            description(R.string.des_auto_clear_ai_cache)
            icon(R.drawable.ic_auto_delete)
            availableWhen(aiCacheEnabled)
        }

        entry(key = SettingsKeys.AiCacheDays) {
            title(R.string.ai_cache_days)
            description(R.string.des_ai_cache_days)
            icon(R.drawable.ic_schedule)
            availableWhen(aiCacheEnabled)
        }

        entry(key = SettingsKeys.AiCacheClear) {
            title(R.string.clear_analysis_cache)
            description(R.string.des_clear_analysis_cache)
            icon(R.drawable.ic_delete_sweep)
        }
    }
}