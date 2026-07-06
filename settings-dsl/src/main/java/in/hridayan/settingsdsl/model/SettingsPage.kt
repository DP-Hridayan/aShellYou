package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import `in`.hridayan.settingsdsl.dsl.settingsPage
import `in`.hridayan.settingsdsl.resolver.resolveAll

/**
 * The top-level container for a settings screen's content.
 *
 * Create instances via the [settingsPage] DSL function. Pass to [SettingsPage.resolveAll]
 * inside a `@Composable` to get renderable [ResolvedGroup]s.
 *
 * @param groups           Internal group specs (populated by DSL).
 * @param screenId         App-defined string identifying this screen for search.
 *                         Null if this page should not appear in search results.
 * @param screenTitleResId String resource ID for the screen's display title (used in search headers).
 *                         Null if [screenId] is null.
 */
@Stable
class SettingsPage internal constructor(
    internal val groups: List<GroupSpec>,
    val screenId: String? = null,
    @param:StringRes val screenTitleResId: Int? = null,
) {
    /**
     * Returns the index of the first group that contains an item with [key], or -1 if not found.
     *
     * This is the *group* index in [groups], not the LazyColumn item index.
     * Use [lazyListIndexOf] for an accurate scroll target.
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
     * Returns the exact LazyColumn item index for [key], accounting for how many lazy items
     * each group contributes:
     *
     * - [GroupSpec.Items]    → item[0], item[1], … (no header row)
     * - [GroupSpec.Category] → header row, item[0], item[1], …
     * - [GroupSpec.Custom]   → 1 lazy item
     * - [GroupSpec.Divider]  → 1 lazy item
     *
     * @param key             The settings key to locate.
     * @param headerItemCount Extra items placed before [groups] in the LazyColumn (e.g. a hero
     *                        header). Added to the returned index.
     * @return The 0-based LazyColumn index of the matching item,
     *         or -1 if [key] is not found.
     */
    fun lazyListIndexOf(key: SettingsKey<*>, headerItemCount: Int = 0): Int {
        var lazyIndex = headerItemCount
        for (group in groups) {
            when (group) {
                is GroupSpec.Items -> {
                    val itemIndex = group.items.indexOfFirst { it.key == key }
                    if (itemIndex >= 0) return lazyIndex + itemIndex
                    lazyIndex += group.items.size
                }

                is GroupSpec.Category -> {
                    val itemIndex = group.items.indexOfFirst { it.key == key }
                    // +1 because the category header row occupies lazyIndex
                    if (itemIndex >= 0) return lazyIndex + 1 + itemIndex
                    lazyIndex += 1 + group.items.size
                }

                is GroupSpec.Custom -> lazyIndex += 1
                GroupSpec.Divider -> lazyIndex += 1
            }
        }
        return -1
    }
}
