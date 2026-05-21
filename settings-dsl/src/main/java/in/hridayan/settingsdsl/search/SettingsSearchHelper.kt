package `in`.hridayan.settingsdsl.search

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import `in`.hridayan.settingsdsl.model.GroupSpec
import `in`.hridayan.settingsdsl.model.ItemSpec
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage

/**
 * A single search result produced by [SettingsSearchEngine].
 *
 * @param key          The [SettingsKey] that identifies this setting.
 * @param title        The resolved display title.
 * @param description  The resolved description. Empty string if none.
 * @param iconResId    Optional drawable resource for the leading icon.
 * @param screenId     App-defined string identifying which screen hosts this item (e.g. `"behavior"`).
 * @param screenTitle  Human-readable title of the parent screen (e.g. `"Behavior"`).
 */
data class SearchEntry(
    val key: SettingsKey<*>,
    val title: String,
    val description: String,
    val iconResId: Int?,
    val screenId: String,
    val screenTitle: String,
)

/**
 * A pre-built, queryable search index over one or more [SettingsPage]s.
 *
 * Create an instance via [SettingsSearchEngine.build] or the composable
 * [rememberSettingsSearch] helper. Once built, call [search] as many times as
 * you like — no `Context` required after construction.
 *
 * ### Example (recommended — page-based)
 * ```kotlin
 * // Pages carry their own screen metadata:
 * val lookAndFeelPage = settingsPage(
 *     screenTitle = R.string.look_and_feel,
 *     screenId = "look_and_feel",
 *     group(switchItem(...), clickableItem(...)),
 * )
 *
 * val engine = SettingsSearchEngine.build(
 *     context = appContext,
 *     pages = listOf(lookAndFeelPage, behaviorPage, ...),
 * )
 * val results = engine.search("font")
 * ```
 *
 * ### Example (manual mapping — legacy)
 * ```kotlin
 * val engine = SettingsSearchEngine.build(
 *     context = appContext,
 *     screens = listOf("look_and_feel" to lookAndFeelPage, ...),
 *     screenTitles = mapOf("look_and_feel" to R.string.look_and_feel, ...),
 * )
 * ```
 */
class SettingsSearchEngine private constructor(
    private val index: List<SearchEntry>,
) {
    /**
     * Returns all [SearchEntry]s whose title or description contains [query]
     * (case-insensitive, trimmed). Empty string returns an empty list.
     */
    fun search(query: String): List<SearchEntry> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return index.filter { entry ->
            entry.title.contains(q, ignoreCase = true) ||
                    entry.description.contains(q, ignoreCase = true)
        }
    }

    /**
     * Returns all [SearchEntry]s regardless of query. Useful for displaying a full list.
     */
    fun allEntries(): List<SearchEntry> = index

    companion object {
        /**
         * Builds a [SettingsSearchEngine] from pages that carry their own screen metadata.
         *
         * Pages without a [SettingsPage.screenId] are silently skipped.
         * This is the **recommended** API — no manual string maps needed.
         *
         * @param context Application context used to resolve string resources.
         * @param pages   List of [SettingsPage]s to index. Each page should have
         *                [SettingsPage.screenId] and [SettingsPage.screenTitleResId] set.
         */
        fun build(
            context: Context,
            pages: List<SettingsPage>,
        ): SettingsSearchEngine {
            val screens = pages.mapNotNull { page ->
                val id = page.screenId ?: return@mapNotNull null
                id to page
            }
            val screenTitles = pages.associate { page ->
                (page.screenId ?: "") to (page.screenTitleResId ?: 0)
            }.filterKeys { it.isNotEmpty() }.filterValues { it != 0 }
            return build(context, screens, screenTitles)
        }

        /**
         * Builds a [SettingsSearchEngine] from the given [screens].
         *
         * This uses [Context.getString] for string resolution — safe to call from a
         * `ViewModel` or any non-composable context.
         *
         * @param context      Application context used to resolve string resources.
         * @param screens      List of `(screenId, SettingsPage)` pairs to index.
         * @param screenTitles Map from screen ID → string resource ID for the screen's display title.
         */
        fun build(
            context: Context,
            screens: List<Pair<String, SettingsPage>>,
            screenTitles: Map<String, Int> = emptyMap(),
        ): SettingsSearchEngine {
            val index = screens.flatMap { (screenId, page) ->
                val screenTitle = screenTitles[screenId]
                    ?.let { context.getString(it) }
                    ?: screenId
                page.groups.flatMap { group ->
                    group.specsOrEmpty()
                        .filter { it.isVisible && it.hasSearchableTitle() }
                        .map { spec ->
                            SearchEntry(
                                key = spec.key,
                                title = spec.titleResId
                                    ?.let { context.getString(it) }
                                    ?: spec.titleString,
                                description = spec.descriptionResId
                                    ?.let { context.getString(it) }
                                    ?: spec.descriptionString,
                                iconResId = spec.iconResId,
                                screenId = screenId,
                                screenTitle = screenTitle,
                            )
                        }
                }
            }
            return SettingsSearchEngine(index)
        }

        private fun GroupSpec.specsOrEmpty(): List<ItemSpec> = when (this) {
            is GroupSpec.Items -> items
            is GroupSpec.Category -> items
            else -> emptyList()
        }

        private fun ItemSpec.hasSearchableTitle(): Boolean =
            titleResId != null || titleString.isNotBlank()
    }
}

/**
 * Composable helper that builds and remembers a [SettingsSearchEngine] from pages
 * that carry their own screen metadata.
 *
 * The engine is rebuilt only when [pages] changes (by reference).
 *
 * @param pages List of [SettingsPage]s to index.
 * @return      A stable [SettingsSearchEngine] ready to call [SettingsSearchEngine.search] on.
 */
@Composable
fun rememberSettingsSearch(
    pages: List<SettingsPage>,
): SettingsSearchEngine {
    val context = LocalContext.current
    return remember(pages) {
        SettingsSearchEngine.build(context = context, pages = pages)
    }
}

/**
 * Composable helper that builds and remembers a [SettingsSearchEngine] for [screens].
 *
 * The engine is rebuilt only when [screens] or [screenTitles] change (by reference).
 * The consumer is responsible for displaying a search text field and passing its value
 * to [SettingsSearchEngine.search]; the module handles all indexing and matching.
 *
 * @param screens      List of `(screenId, SettingsPage)` pairs to index.
 * @param screenTitles Map from screen ID → string resource ID for the screen's display title.
 * @return             A stable [SettingsSearchEngine] ready to call [SettingsSearchEngine.search] on.
 */
@Composable
fun rememberSettingsSearch(
    screens: List<Pair<String, SettingsPage>>,
    screenTitles: Map<String, Int> = emptyMap(),
): SettingsSearchEngine {
    val context = LocalContext.current
    return remember(screens, screenTitles) {
        SettingsSearchEngine.build(
            context = context,
            screens = screens,
            screenTitles = screenTitles,
        )
    }
}
