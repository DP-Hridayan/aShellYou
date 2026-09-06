package `in`.hridayan.settingsdsl.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

@DslMarker
annotation class SearchDslMarker

/**
 * Declares the settings search index as a graph of screens, mirroring the navigation graph.
 *
 * Each [SearchGraphScope.screen] owns the navigation lambda shared by the entries inside it, and
 * screens nest to match the real navigation hierarchy.
 *
 * Availability is evaluated eagerly: unavailable screens and entries are absent from the returned
 * graph rather than filtered later. Read the gates in composition and rebuild the graph when one
 * changes, so an unreachable setting can never appear in results.
 *
 * Example:
 * ```kotlin
 * val graph = remember(navController, aiEnabled) {
 *     searchGraph {
 *         screen(SCREEN_ID_SETTINGS, R.string.settings, { navController.navigate(NavRoutes.SettingsScreen) }) {
 *             entry(SettingsKeys.LookAndFeel, R.string.look_and_feel, R.string.des_look_and_feel)
 *
 *             screen(SCREEN_ID_AI, R.string.ai_models, { navController.navigate(NavRoutes.AiModelsScreen) }) {
 *                 availableWhen(aiEnabled)
 *                 entry(SettingsKeys.AiCacheEnabled, R.string.ai_cache_enabled)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param block Builder block declaring the screens in this index.
 */
fun searchGraph(block: SearchGraphScope.() -> Unit): SearchGraph =
    SearchGraphScope().apply(block).build()

/** Scope for declaring the top-level screens of a [SearchGraph]. */
@SearchDslMarker
class SearchGraphScope internal constructor() {
    private val screens = mutableListOf<SearchScreenScope>()

    /**
     * Declares a screen and the entries it hosts.
     *
     * @param id Stable identifier, unique within the graph. Must not change between builds.
     * @param title String resource naming the screen in search results.
     * @param navigate Invoked when the user taps a result belonging to this screen.
     * @param block Builder block declaring this screen's entries and nested screens.
     */
    fun screen(
        id: String,
        @StringRes title: Int,
        navigate: () -> Unit,
        block: SearchScreenScope.() -> Unit,
    ) {
        screens.add(SearchScreenScope(id, title, navigate).apply(block))
    }

    internal fun build(): SearchGraph = SearchGraph(screens.flatMap { it.flatten() })
}

/** Scope for declaring the entries and nested screens of a single search screen. */
@SearchDslMarker
class SearchScreenScope internal constructor(
    private val id: String,
    @param:StringRes private val titleRes: Int,
    private val navigate: () -> Unit,
) {
    private var available = true
    private val entries = mutableListOf<SearchEntryNode>()
    private val nested = mutableListOf<SearchScreenScope>()

    /**
     * Drops this screen, its entries and every nested screen from the index when [condition] is
     * false. Use this for build flavours and hardware capabilities that make a whole screen
     * unreachable.
     */
    fun availableWhen(condition: Boolean) {
        available = condition
    }

    /**
     * Declares one searchable setting.
     *
     * @param key Must equal the key used by the matching item in the UI DSL.
     * @param title String resource for the result title.
     * @param description String resource for the result subtitle.
     * @param icon Drawable resource for the leading icon.
     * @param availableWhen Drops this entry from the index when false.
     * @param keywords String resources matched against the query but never displayed.
     */
    fun entry(
        key: Any,
        @StringRes title: Int,
        @StringRes description: Int? = null,
        @DrawableRes icon: Int? = null,
        availableWhen: Boolean = true,
        @StringRes keywords: List<Int> = emptyList(),
    ) {
        if (!availableWhen) return
        entries.add(
            SearchEntryNode(
                key = key,
                titleRes = title,
                descriptionRes = description,
                iconRes = icon,
                keywordRes = keywords,
            )
        )
    }

    /**
     * Declares a screen reachable from this one. A nested screen inherits this screen's
     * availability, so gating a parent removes its whole subtree.
     */
    fun screen(
        id: String,
        @StringRes title: Int,
        navigate: () -> Unit,
        block: SearchScreenScope.() -> Unit,
    ) {
        nested.add(SearchScreenScope(id, title, navigate).apply(block))
    }

    internal fun flatten(): List<SearchScreenNode> {
        if (!available) return emptyList()
        val self = if (entries.isEmpty()) {
            emptyList()
        } else {
            listOf(
                SearchScreenNode(
                    id = id,
                    titleRes = titleRes,
                    navigate = navigate,
                    entries = entries.toList(),
                )
            )
        }
        return self + nested.flatMap { it.flatten() }
    }
}
