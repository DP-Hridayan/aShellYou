package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes

/**
 * Internal blueprint for a group of settings items.
 * Created by [group], [customSlot], [divider] DSL functions.
 */
internal sealed class GroupSpec {
    /**
     * Represents a grouping of settings items.
     * If [titleResId] or [title] is provided, it renders with a category header.
     * Otherwise, it renders as an uncategorized block of items.
     */
    data class Group(
        @param:StringRes val titleResId: Int?,
        val title: String,
        val items: List<ItemSpec>
    ) : GroupSpec()

    /**
     * Represents a custom composable slot injected into the settings page.
     */
    data class Custom(val slot: CustomSlot) : GroupSpec()

    /**
     * Represents a visual divider between groups.
     */
    data object Divider : GroupSpec()
}
