package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.page.search.SettingsSearchIndex

/**
 * A flat, searchable representation of a single settings preference item.
 *
 * Built by [SettingsSearchIndex] at ViewModel init time by flattening all
 * [SettingsProvider.*PageList] hierarchies into a single list with pre-resolved
 * title/description strings (so search works without @Composable context).
 */
data class SearchableSettingsEntry(
    val settingsKey: SettingsKeys,
    val title: String,
    val description: String,
    val iconResId: Int? = null,
    val screenId: SettingsScreenId,
    val parentScreenTitle: String,
)
