package `in`.hridayan.settingsdsl.search

import android.content.Context
import androidx.annotation.DrawableRes

private const val ID_SEPARATOR = "/"
private const val HAYSTACK_SEPARATOR = " "

/**
 * A resolved, display-ready search result.
 *
 * @param key The raw item key, passed to the highlight state so the destination screen can scroll
 *            to and pulse the matching row.
 * @param id Screen-qualified identifier, unique across the index and stable across builds. Use
 *           this for persistence; [key] alone is not unique, as one setting may be reachable from
 *           more than one screen.
 * @param title Resolved result title.
 * @param description Resolved result subtitle. Empty when the entry declares none.
 * @param iconResId Drawable for the leading icon, or null to fall back to a generic icon.
 * @param screenTitle Resolved name of the hosting screen, used as the result section header.
 * @param navigateTo Navigates to the hosting screen.
 */
class SearchResult internal constructor(
    val key: Any,
    val id: String,
    val title: String,
    val description: String,
    @param:DrawableRes val iconResId: Int?,
    val screenTitle: String,
    val navigateTo: () -> Unit,
    internal val haystack: String,
)

/**
 * A pre-resolved, queryable index over one [SearchGraph].
 *
 * All string resources are resolved once at construction, so querying needs no `Context` and is
 * safe to drive from a `ViewModel`. The graph is already pruned of unavailable screens and
 * entries, so every result is reachable.
 */
class SettingsSearchEngine private constructor(private val index: List<SearchResult>) {

    /**
     * Returns the entries whose title, description or keywords contain [query], trimmed and
     * case-insensitive. A blank query returns nothing.
     */
    fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return index.filter { it.haystack.contains(trimmed, ignoreCase = true) }
    }

    /** Returns every indexed entry. */
    fun allEntries(): List<SearchResult> = index

    /**
     * Resolves persisted [SearchResult.id]s back to results, preserving the given order and
     * dropping any that are no longer in the index.
     */
    fun resolve(ids: List<String>): List<SearchResult> {
        val byId = index.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    companion object {
        /** Builds an index from [graph], resolving every string resource through [context]. */
        fun build(context: Context, graph: SearchGraph): SettingsSearchEngine =
            SettingsSearchEngine(graph.screens.flatMap { screen -> screen.toResults(context) })

        private fun SearchScreenNode.toResults(context: Context): List<SearchResult> {
            val resolvedScreenTitle = context.getString(titleRes)
            return entries.map { entry -> entry.toResult(context, this, resolvedScreenTitle) }
        }

        private fun SearchEntryNode.toResult(
            context: Context,
            screen: SearchScreenNode,
            resolvedScreenTitle: String,
        ): SearchResult {
            val resolvedTitle = context.getString(titleRes)
            val resolvedDescription = descriptionRes?.let(context::getString).orEmpty()
            val resolvedKeywords = keywordRes.joinToString(HAYSTACK_SEPARATOR, transform = context::getString)
            return SearchResult(
                key = key,
                id = screen.id + ID_SEPARATOR + key,
                title = resolvedTitle,
                description = resolvedDescription,
                iconResId = iconRes,
                screenTitle = resolvedScreenTitle,
                navigateTo = screen.navigate,
                haystack = listOf(resolvedTitle, resolvedDescription, resolvedKeywords)
                    .filter { it.isNotEmpty() }
                    .joinToString(HAYSTACK_SEPARATOR),
            )
        }
    }
}
