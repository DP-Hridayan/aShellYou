package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * The top-level container produced by the [in.hridayan.settingsdsl.dsl.SettingsGraphBuilder] DSL.
 *
 * A [SettingsGraph] is the single source of truth for both the global search engine
 * and the [in.hridayan.settingsdsl.ui.SettingsColumn] UI renderer.
 *
 * Create instances via the [in.hridayan.settingsdsl.dsl.settingsGraph] DSL function.
 *
 * @param groups Internal list of group nodes that make up this graph.
 * @param screenTitleResId String resource ID for the display title of the screen that hosts this graph.
 *                         Used by the search engine to label results with their parent screen name.
 *                         Null if this graph should not be indexed for search.
 * @param navigateTo Lambda invoked by the search engine when the user taps a result belonging to this graph.
 *                   Capture your [androidx.navigation.NavController] here.
 */
@Stable
class SettingsGraph internal constructor(
    internal val groups: List<SettingsGraphGroup>,
    @StringRes val screenTitleResId: Int? = null,
    val navigateTo: () -> Unit = {},
)

/**
 * Internal sealed representation of a single entry in a [SettingsGraph].
 *
 * Produced exclusively by [in.hridayan.settingsdsl.dsl.SettingsGraphBuilder] builder functions.
 * Never exposed directly to screen-level code.
 */
internal sealed class SettingsGraphGroup {

    /**
     * A named or unnamed group of settings nodes.
     *
     * @param titleResId Optional string resource for the group header label.
     * @param titleString Optional plain-string group header label.
     * @param nodes The list of setting nodes belonging to this group.
     */
    data class Group(
        @StringRes val titleResId: Int?,
        val titleString: String,
        val nodes: List<SettingsNode>,
    ) : SettingsGraphGroup()

    /**
     * A custom composable slot injected between groups.
     *
     * @param slot The [CustomSlot] identifier used to dispatch rendering in [in.hridayan.settingsdsl.ui.SettingsColumn].
     */
    data class Custom(val slot: CustomSlot) : SettingsGraphGroup()

    /**
     * An arbitrary composable item injected at this position in the list.
     *
     * @param key Stable unique key for the lazy list item.
     * @param content The composable content to render.
     */
    class RawItem(val key: Any, val content: @Composable () -> Unit) : SettingsGraphGroup()

    /** A horizontal visual divider between groups. */
    data object Divider : SettingsGraphGroup()
}

