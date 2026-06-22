package `in`.hridayan.settingsdsl.ui.highlight

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage
import kotlinx.coroutines.delay

/**
 * Manages scroll-to-item + blink-highlight state for a DSL-based settings sub-screen.
 *
 * Call this once in each sub-screen that supports deep-link highlighting.
 * Returns the currently highlighted [SettingsKey] (or null) — pass the returned value
 * as `highlightedKey` to [resolveAll] / [settingsContent] to drive the card color blink.
 *
 * @param highlightKeyName The raw key name string passed in via nav args (nullable).
 * @param page             The [SettingsPage] to search for the target key index.
 * @param listState        The lazy list state used to animate the scroll.
 * @param headerItemCount  Number of non-group items before the first group in the LazyColumn.
 * @param keyResolver      Resolves a key name string to a [SettingsKey]. Apps should pass
 *                         their own lookup function (e.g. `SettingsKeys::valueOfOrNull`).
 */
@Composable
fun rememberHighlightState(
    highlightKeyName: String?,
    page: SettingsPage,
    listState: LazyListState,
    headerItemCount: Int = 0,
    keyResolver: (String) -> SettingsKey<*>? = { null },
): SettingsKey<*>? {
    // Tracks whether we are still showing the highlight (visual blink)
    var highlightedKey by rememberSaveable { mutableStateOf(highlightKeyName) }

    // One-shot scroll: fires only when highlightKeyName arrives, never again.
    // Kept separate from the highlight-clear timer so clearing the highlight
    // does NOT cause a second recomposition-driven scroll.
    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        val targetKey = keyResolver(highlightKeyName) ?: return@LaunchedEffect

        // Use lazyListIndexOf for an accurate item index (counts headers, category labels, etc.)
        val lazyIndex = page.lazyListIndexOf(targetKey, headerItemCount)
        if (lazyIndex >= 0) {
            delay(400) // let the screen enter-transition finish
            listState.animateScrollToItem(index = lazyIndex)
        }
    }

    // Auto-clear highlight after 2 seconds — isolated effect so it never triggers a scroll.
    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        delay(2000)
        highlightedKey = null
    }

    return highlightedKey?.let { keyResolver(it) }
}
