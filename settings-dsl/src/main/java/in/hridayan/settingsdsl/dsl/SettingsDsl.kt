package `in`.hridayan.settingsdsl.dsl

import androidx.annotation.StringRes
import `in`.hridayan.settingsdsl.model.SettingsGraph

/**
 * Creates a [SettingsGraph] — the single source of truth for settings UI and global search.
 *
 * The [block] is executed immediately to build the graph structure. All group and item
 * definitions go inside this block.
 *
 * Example:
 * ```kotlin
 * val appearanceGraph = settingsGraph(
 *     screenTitleRes = R.string.appearance,
 *     navigateTo = { navController.navigate(NavRoutes.AppearanceScreen) },
 * ) {
 *     group(R.string.theme) {
 *         switchItem(key = SettingsKeys.DarkMode) {
 *             title(R.string.dark_mode)
 *             description(R.string.dark_mode_description)
 *         }
 *     }
 * }
 * ```
 *
 * @param screenTitleRes String resource ID for the screen title shown in search results.
 *                       Null if this graph should not be indexed for search.
 * @param navigateTo Lambda invoked when the user taps a search result for this graph.
 *                   Capture your NavController here.
 * @param block Builder block for defining groups and items.
 */
fun settingsGraph(
    @StringRes screenTitleRes: Int? = null,
    navigateTo: () -> Unit = {},
    block: SettingsGraphBuilder.() -> Unit,
): SettingsGraph = SettingsGraphBuilder(
    screenTitleResId = screenTitleRes,
    navigateTo = navigateTo,
).apply(block).build()

