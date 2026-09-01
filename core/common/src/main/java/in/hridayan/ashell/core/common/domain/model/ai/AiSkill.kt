package `in`.hridayan.ashell.core.common.domain.model.ai

import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.resources.R

enum class AiSkill(
    val displayNameRes: Int,
    val descriptionRes: Int,
    val settingsKey: SettingsKeys<Boolean>
) {
    COMMAND_EXECUTION(
        R.string.command_execution,
        R.string.des_command_execution,
        SettingsKeys.AiSkillCommandExecution
    ),
    QUICK_SETTINGS(
        R.string.quick_settings_tiles,
        R.string.des_quick_settings_tiles,
        SettingsKeys.AiSkillQuickSettings
    ),
    PACKAGES(
        R.string.packages,
        R.string.des_packages,
        SettingsKeys.AiSkillPackages
    ),
    DATABASE(
        R.string.database_modification,
        R.string.des_database_modification,
        SettingsKeys.AiSkillDatabase
    )
}
