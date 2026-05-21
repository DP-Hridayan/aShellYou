package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes

/**
 * A resolved group ready for rendering.
 *
 * Produced by [SettingsPage.resolveAll]. Each [ResolvedGroup] holds either a list of
 * resolved [SettingsItem]s (with card shapes already computed), a custom slot marker,
 * or a divider marker.
 */
sealed interface ResolvedGroup {

    /**
     * A group of resolved items, optionally with a category header.
     *
     * @param categoryTitle Already-resolved string for the category header, or null if uncategorized.
     * @param items Visibility-filtered list of [SettingsItem]s with auto-computed shapes.
     */
    data class ItemGroup(
        val categoryTitle: String?,
        val items: List<SettingsItem>,
    ) : ResolvedGroup

    /** A custom composable placeholder identified by its [CustomSlot]. */
    data class Custom(val slot: CustomSlot) : ResolvedGroup

    /** A horizontal visual divider between groups. */
    data object Divider : ResolvedGroup
}
