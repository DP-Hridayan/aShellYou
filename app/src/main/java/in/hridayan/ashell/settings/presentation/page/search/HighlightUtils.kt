package `in`.hridayan.ashell.settings.presentation.page.search

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.settingsdsl.model.SettingsPage
import kotlinx.coroutines.delay

/**
 * Manages scroll-to-item + blink-highlight state for a DSL-based settings sub-screen.
 *
 * Call this once in each sub-screen that supports deep-link highlighting.
 * Returns the currently highlighted [SettingsKeys] (or null) — pass the returned value
 * as `highlightedKey` to [settingsContent] to drive the card color blink.
 *
 * @param highlightKeyName The raw enum name string passed in via nav args (nullable).
 * @param page             The [SettingsPage] to search for the target key index.
 * @param listState        The lazy list state used to animate the scroll.
 * @param headerItemCount  Number of non-group items before the first group in the LazyColumn.
 */
@Composable
fun rememberHighlightState(
    highlightKeyName: String?,
    page: SettingsPage,
    listState: LazyListState,
    headerItemCount: Int = 0,
): SettingsKeys? {
    var highlightedKey by rememberSaveable { mutableStateOf(highlightKeyName) }

    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        val targetKey = runCatching { SettingsKeys.valueOf(highlightKeyName) }.getOrNull()
            ?: return@LaunchedEffect

        // Uses the public DSL API — does NOT touch internal GroupSpec/ItemSpec
        val groupIndex = page.indexOfGroupContaining(targetKey)
        if (groupIndex >= 0) {
            delay(600) // let screen transition finish so scroll animation is visible
            listState.animateScrollToItem(headerItemCount + groupIndex)
        }
    }

    // Auto-clear highlight after 2.5 seconds
    LaunchedEffect(highlightedKey) {
        if (highlightedKey != null) {
            delay(2500)
            highlightedKey = null
        }
    }

    return highlightedKey?.let { runCatching { SettingsKeys.valueOf(it) }.getOrNull() }
}
