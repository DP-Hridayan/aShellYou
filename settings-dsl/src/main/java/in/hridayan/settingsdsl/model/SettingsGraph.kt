package `in`.hridayan.settingsdsl.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * The rendering plan produced by the [in.hridayan.settingsdsl.dsl.SettingsGraphBuilder] DSL and
 * consumed by [in.hridayan.settingsdsl.ui.SettingsColumn].
 *
 * Built fresh on every recomposition from the DSL block, so it always reflects the latest values
 * captured by the enclosing composable. The search index is declared separately, via
 * [in.hridayan.settingsdsl.search.searchGraph].
 *
 * @param groups The group nodes that make up this screen.
 */
@Stable
internal class SettingsGraph internal constructor(
    internal val groups: List<SettingsGraphGroup>,
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
     * An arbitrary composable item injected at this position in the list.
     *
     * @param key Stable unique key for the lazy list item.
     * @param content The composable content to render.
     */
    class RawItem(val key: Any, val content: @Composable () -> Unit) : SettingsGraphGroup()

    /** A horizontal visual divider between groups. */
    data object Divider : SettingsGraphGroup()
}

