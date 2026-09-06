package `in`.hridayan.settingsgraph.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

@DslMarker
annotation class SearchGraphMarker

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
 *             entry(SettingsKeys.LookAndFeel) {
 *                 title(R.string.look_and_feel)
 *                 description(R.string.des_look_and_feel)
 *                 icon(R.drawable.ic_pallete)
 *             }
 *
 *             screen(SCREEN_ID_AI, R.string.ai_models, { navController.navigate(NavRoutes.AiModelsScreen) }) {
 *                 availableWhen(aiEnabled)
 *                 entry(SettingsKeys.AiCacheEnabled) {
 *                     title(R.string.ai_cache_enabled)
 *                 }
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
@SearchGraphMarker
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
@SearchGraphMarker
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
     * @param key Must equal the key used by the matching item in the UI graph.
     * @param block Builder block for configuring the entry properties.
     */
    fun entry(key: Any, block: SearchEntryScope.() -> Unit) {
        val node = SearchEntryScope(key).apply(block).build()
        if (node != null) entries.add(node)
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

/** Scope for configuring a single search entry. */
@SearchGraphMarker
class SearchEntryScope internal constructor(private val key: Any) {
    private var availableWhen: Boolean = true
    private var titleRes: Int? = null
    private var titleString: String? = null
    private var descriptionRes: Int? = null
    private var descriptionString: String? = null
    private var iconRes: Int? = null
    private var iconVector: ImageVector? = null
    private var keywordRes = mutableListOf<Int>()
    private var keywordStrings = mutableListOf<String>()

    /** Sets the result title using a string resource. */
    fun title(@StringRes resId: Int) {
        titleRes = resId
        titleString = null
    }

    /** Sets the result title using a hardcoded string. */
    fun title(text: String) {
        titleString = text
        titleRes = null
    }

    /** Sets the result subtitle using a string resource. */
    fun description(@StringRes resId: Int) {
        descriptionRes = resId
        descriptionString = null
    }

    /** Sets the result subtitle using a hardcoded string. */
    fun description(text: String) {
        descriptionString = text
        descriptionRes = null
    }

    /** Sets the leading icon using a drawable resource. */
    fun icon(@DrawableRes resId: Int) {
        iconRes = resId
        iconVector = null
    }

    /** Sets the leading icon using an ImageVector. */
    fun icon(vector: ImageVector) {
        iconVector = vector
        iconRes = null
    }

    /**
     * Drops this entry from the index when [condition] is false. Use this for feature flags
     * or hardware capabilities that make this specific setting unreachable.
     */
    fun availableWhen(condition: Boolean) {
        availableWhen = condition
    }

    /**
     * Adds string resources or hardcoded strings matched against the query but never displayed.
     * Use these for option labels of items that render without a title, such as radio groups.
     *
     * @param keywords A mix of [Int] (StringRes) and [String]s.
     */
    fun keywords(vararg keywords: Any) {
        for (kw in keywords) {
            when (kw) {
                is Int -> keywordRes.add(kw)
                is String -> keywordStrings.add(kw)
                else -> throw IllegalArgumentException("Keywords must be String or Int (StringRes)")
            }
        }
    }

    internal fun build(): SearchEntryNode? {
        if (!availableWhen) return null
        return SearchEntryNode(
            key = key,
            titleRes = titleRes,
            titleString = titleString,
            descriptionRes = descriptionRes,
            descriptionString = descriptionString,
            iconRes = iconRes,
            iconVector = iconVector,
            keywordRes = keywordRes,
            keywordStrings = keywordStrings,
        )
    }
}
