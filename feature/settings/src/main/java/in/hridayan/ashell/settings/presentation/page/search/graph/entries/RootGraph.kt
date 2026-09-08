package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.rootGraph(aiEnabled: Boolean) {
    entry(key = SettingsKeys.LookAndFeel) {
        title(R.string.look_and_feel)
        description(R.string.des_look_and_feel)
        icon(R.drawable.ic_pallete)
    }

    entry(key = SettingsKeys.Behavior) {
        title(R.string.behavior)
        description(R.string.des_behavior)
        icon(R.drawable.ic_sentiment_neutral)
    }

    entry(key = SettingsKeys.QuickSettingsTiles) {
        title(R.string.qs_tiles)
        description(R.string.des_qs_tiles)
        icon(R.drawable.ic_dashboard)
    }

    entry(key = SettingsKeys.AiModels) {
        title(R.string.ai_models)
        description(R.string.des_ai_models)
        icon(R.drawable.ic_cloud_model)
        availableWhen(aiEnabled)
    }

    entry(key = SettingsKeys.AutoUpdate) {
        title(R.string.auto_update)
        description(R.string.des_auto_update)
        icon(R.drawable.ic_auto_update)
    }

    entry(key = SettingsKeys.BackupAndRestore) {
        title(R.string.backup_and_restore)
        description(R.string.des_backup_and_restore)
        icon(R.drawable.ic_settings_backup_restore)
    }

    entry(key = SettingsKeys.About) {
        title(R.string.about)
        description(R.string.des_about)
        icon(R.drawable.ic_info)
    }
}