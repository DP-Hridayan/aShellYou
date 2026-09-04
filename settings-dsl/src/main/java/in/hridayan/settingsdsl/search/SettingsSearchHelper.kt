package `in`.hridayan.settingsdsl.search

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import `in`.hridayan.settingsdsl.model.SettingsGraph
import `in`.hridayan.settingsdsl.model.SettingsGraphGroup

/**
 * A single search result produced by [SettingsSearchEngine].
 *
 * @param key          The key that identifies this setting.
 * @param title        The resolved display title.
 * @param description  The resolved description. Empty string if none.
 * @param iconResId    Optional drawable resource for the leading icon.
 * @param screenTitle  Human-readable title of the parent screen (e.g. "Behavior").
 * @param navigateTo   Lambda invoked to navigate to the item's screen.
 * @param isVisible    Lambda invoked to determine if this item is visible.
 */
@Immutable
data class SearchEntry(
    val key: Any,
    val title: String,
    val description: String,
    val iconResId: Int?,
    val screenTitle: String,
    val navigateTo: () -> Unit,
    val isVisible: () -> Boolean,
)

/**
 * A pre-built, queryable search index over one or more [SettingsGraph]s.
 */
class SettingsSearchEngine private constructor(
    private val index: List<SearchEntry>,
) {
    /**
     * Returns all [SearchEntry]s whose title or description contains [query]
     * (case-insensitive, trimmed) and where [SearchEntry.isVisible] returns true.
     * Empty string returns an empty list.
     */
    fun search(query: String): List<SearchEntry> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return index.filter { entry ->
            entry.isVisible() && (entry.title.contains(
                q,
                ignoreCase = true
            ) || entry.description.contains(q, ignoreCase = true))
        }
    }

    /**
     * Returns all [SearchEntry]s regardless of query.
     */
    fun allEntries(): List<SearchEntry> = index

    companion object {
        /**
         * Builds a [SettingsSearchEngine] from a list of [SettingsGraph]s.
         */
        fun build(
            context: Context,
            graphs: List<SettingsGraph>,
        ): SettingsSearchEngine {
            val index = graphs.flatMap { graph ->
                val screenTitle = graph.screenTitleResId?.let { context.getString(it) } ?: ""
                graph.groups.filterIsInstance<SettingsGraphGroup.Group>().flatMap { group ->
                    group.nodes.filter { node ->
                        node.searchTitleRes != null || node.staticTitleRes != null || node.staticTitleString.isNotBlank()
                    }.map { node ->
                        SearchEntry(
                            key = node.key,
                            title = node.searchTitleRes?.let { context.getString(it) }
                                ?: node.staticTitleRes?.let { context.getString(it) }
                                ?: node.staticTitleString,
                            description = node.searchDescRes?.let { context.getString(it) }
                                ?: node.staticDescRes?.let { context.getString(it) }
                                ?: node.staticDescString,
                            iconResId = node.iconResId,
                            screenTitle = screenTitle,
                            navigateTo = graph.navigateTo,
                            isVisible = node.isVisible
                        )
                    }
                }
            }
            return SettingsSearchEngine(index)
        }
    }
}

/**
 * Composable helper that builds and remembers a [SettingsSearchEngine] from graphs.
 */
@Composable
fun rememberSettingsSearch(vararg graphs: SettingsGraph): SettingsSearchEngine {
    val context = LocalContext.current
    return remember(graphs) {
        SettingsSearchEngine.build(context, graphs.toList())
    }
}
