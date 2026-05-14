package `in`.hridayan.ashell.settings.presentation.search

import android.content.Context
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SearchableSettingsEntry
import `in`.hridayan.ashell.settings.domain.model.SettingsScreenId
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import `in`.hridayan.ashell.settings.presentation.model.PreferenceItem
import `in`.hridayan.ashell.settings.presentation.provider.SettingsProvider

/**
 * Builds a flat, searchable index of every preference item across all settings screens.
 *
 * String resources are resolved eagerly using the application [Context] so the index
 * can be queried from a ViewModel without requiring @Composable context.
 */
object SettingsSearchIndex {

    /** Screen definition used during index construction. */
    private data class ScreenDef(
        val id: SettingsScreenId,
        val titleResId: Int,
        val pageList: List<PreferenceGroup>,
    )

    fun build(context: Context): List<SearchableSettingsEntry> {
        val screens = listOf(
            ScreenDef(SettingsScreenId.SETTINGS_MAIN, R.string.settings, SettingsProvider.settingsPageList),
            ScreenDef(SettingsScreenId.LOOK_AND_FEEL, R.string.look_and_feel, SettingsProvider.lookAndFeelPageList),
            ScreenDef(SettingsScreenId.DARK_THEME, R.string.dark_theme, SettingsProvider.darkThemePageList),
            ScreenDef(SettingsScreenId.BEHAVIOR, R.string.behavior, SettingsProvider.behaviorPageList),
            ScreenDef(SettingsScreenId.AUTO_UPDATE, R.string.auto_update, SettingsProvider.autoUpdatePageList),
            ScreenDef(SettingsScreenId.ABOUT, R.string.about, SettingsProvider.aboutPageList),
            ScreenDef(SettingsScreenId.BACKUP_AND_RESTORE, R.string.backup_and_restore, SettingsProvider.backupPageList),
        )

        return screens.flatMap { screen ->
            val parentTitle = context.getString(screen.titleResId)
            screen.pageList.flatMap { group ->
                val items = when (group) {
                    is PreferenceGroup.Items -> group.items
                    is PreferenceGroup.Category -> group.items
                    else -> emptyList()
                }
                items
                    .filter { it.isLayoutVisible }
                    .filter { it.hasSearchableTitle() }
                    .map { item ->
                        SearchableSettingsEntry(
                            settingsKey = item.key,
                            title = item.resolveTitle(context),
                            description = item.resolveDescription(context),
                            iconResId = item.iconResId,
                            screenId = screen.id,
                            parentScreenTitle = parentTitle,
                        )
                    }
            }
        }
    }

    /** Returns true if the item has a displayable title (not a radio group or empty). */
    private fun PreferenceItem.hasSearchableTitle(): Boolean =
        titleResId != null || titleString.isNotBlank()

    private fun PreferenceItem.resolveTitle(context: Context): String =
        titleResId?.let { context.getString(it) } ?: titleString

    private fun PreferenceItem.resolveDescription(context: Context): String =
        descriptionResId?.let { context.getString(it) } ?: descriptionString
}
