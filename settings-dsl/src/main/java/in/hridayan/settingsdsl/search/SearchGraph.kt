package `in`.hridayan.settingsdsl.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * A single searchable setting.
 *
 * Search matches on the title, the description and any [keywordRes]; only the title and
 * description are displayed. Where a result navigates to is owned by the enclosing
 * [SearchScreenNode], not by the entry.
 *
 * @param key Must equal the key passed to the matching item builder in the UI DSL — it is what
 *            scroll-to-and-highlight matches on after navigating.
 * @param titleRes String resource for the result title.
 * @param descriptionRes String resource for the result subtitle, or null for none.
 * @param iconRes Drawable resource for the leading icon, or null to fall back to a generic icon.
 * @param keywordRes String resources matched against the query but never displayed. Use these for
 *                   option labels of items that render without a title, such as radio groups.
 */
class SearchEntryNode internal constructor(
    val key: Any,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int?,
    @param:DrawableRes val iconRes: Int?,
    @param:StringRes val keywordRes: List<Int>,
)

/**
 * A screen hosting a set of [SearchEntryNode]s.
 *
 * @param id Stable identifier for this screen. Must not change between builds — it qualifies
 *           persisted recent-search entries, which resource ids cannot do safely.
 * @param titleRes String resource for the screen name, shown as the result section header.
 * @param navigate Invoked when the user taps any result belonging to this screen.
 * @param entries The searchable settings hosted by this screen.
 */
class SearchScreenNode internal constructor(
    val id: String,
    @param:StringRes val titleRes: Int,
    val navigate: () -> Unit,
    val entries: List<SearchEntryNode>,
)

/**
 * A flattened settings search index.
 *
 * Availability is resolved when the graph is built, so this contains only reachable screens and
 * entries. Rebuild the graph when a gate changes rather than filtering at query time.
 *
 * Create instances via [searchGraph].
 */
class SearchGraph internal constructor(val screens: List<SearchScreenNode>)
