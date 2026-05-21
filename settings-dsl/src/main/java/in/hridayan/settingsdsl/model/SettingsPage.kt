package `in`.hridayan.settingsdsl.model

/**
 * The top-level container for a settings screen's content.
 *
 * Create instances via the [settingsPage] DSL function. Pass to [SettingsPage.resolveAll]
 * inside a `@Composable` to get renderable [ResolvedGroup]s.
 */
class SettingsPage internal constructor(
    internal val groups: List<GroupSpec>,
) {
    /**
     * Returns the index of the first group that contains an item with [key], or -1 if not found.
     *
     * Used by scroll-to-highlight logic to find which LazyColumn item to animate to.
     */
    fun indexOfGroupContaining(key: SettingsKey): Int =
        groups.indexOfFirst { group ->
            val items: List<ItemSpec> = when (group) {
                is GroupSpec.Items -> group.items
                is GroupSpec.Category -> group.items
                else -> emptyList()
            }
            items.any { it.key == key }
        }
}
