package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes

/**
 * The top-level container for a settings screen's content.
 *
 * Create instances via the [settingsPage] DSL function. Pass to [SettingsPage.resolveAll]
 * inside a `@Composable` to get renderable [ResolvedGroup]s.
 *
 * @param groups           Internal group specs (populated by DSL).
 * @param screenId         App-defined string identifying this screen for search.
 *                         Null if this page should not appear in search results.
 * @param screenTitleResId String resource ID for the screen's display title (used in search headers).\
 *                         Null if [screenId] is null.
 */
class SettingsPage internal constructor(
    internal val groups: List<GroupSpec>,
    val screenId: String? = null,
    @param:StringRes val screenTitleResId: Int? = null,
) {
    /**
     * Returns the index of the first group that contains an item with [key], or -1 if not found.
     *
     * This is the *group* index in [groups], not the LazyColumn item index.
     * Use [lazyListIndexOf] for the accurate scroll target.
     */
    fun indexOfGroupContaining(key: SettingsKey<*>): Int =
        groups.indexOfFirst { group ->
            val items: List<ItemSpec> = when (group) {
                is GroupSpec.Items -> group.items
                is GroupSpec.Category -> group.items
                else -> emptyList()
            }
            items.any { it.key == key }
        }

    /**
     * Returns the actual LazyColumn item index for the group that contains [key],
     * accounting for the real lazy item count each group contributes:
     *
     * - [GroupSpec.Items]    → items.size lazy items (no header)
     * - [GroupSpec.Category] → 1 (category header) + items.size lazy items
     * - [GroupSpec.Custom]   → 1 lazy item
     * - [GroupSpec.Divider]  → 1 lazy item
     *
     * @param key             The settings key to locate.
     * @param headerItemCount Extra items placed before [groups] in the LazyColumn
     *                        (e.g. a hero header). Added to the returned index.
     * @return The 0-based LazyColumn index of the first item of the matching group,
     *         or -1 if [key] is not found.
     */
    fun lazyListIndexOf(key: SettingsKey<*>, headerItemCount: Int = 0): Int {
        var lazyIndex = headerItemCount
        for (group in groups) {
            val containsKey = when (group) {
                is GroupSpec.Items -> group.items.any { it.key == key }
                is GroupSpec.Category -> group.items.any { it.key == key }
                else -> false
            }
            if (containsKey) return lazyIndex
            // Advance by however many lazy items this group contributes
            lazyIndex += when (group) {
                is GroupSpec.Items -> group.items.size
                is GroupSpec.Category -> 1 + group.items.size  // header + items
                is GroupSpec.Custom -> 1
                GroupSpec.Divider -> 1
            }
        }
        return -1
    }
}
