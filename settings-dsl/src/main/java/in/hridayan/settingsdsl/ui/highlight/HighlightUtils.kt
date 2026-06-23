package `in`.hridayan.settingsdsl.ui.highlight

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
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
 * **Design note — why the blink does not need a state-clearing timer:**
 * The card-level blink animation ([highlightCardColors]) is finite: it plays 3 pulses
 * (~2.4 s) via [Animatable] and then returns to the normal color on its own WITHOUT
 * ever changing [isHighlighted] or [resolvedGroups]. Keeping the state immutable after
 * the initial scroll means the [LazyColumn] never recomposes solely due to the blink
 * ending, so `animateItem()` placement animations are never triggered for items near
 * the top of the list.
 *
 * [highlightedKey] IS cleared immediately when [hasScrolled] is already true on entry
 * (composable was recreated mid-blink, e.g. via config change or Compose disposing the
 * back-stack entry under memory pressure), so orphaned indefinite blinks are impossible.
 *
 * @param highlightKeyName The raw key name string passed in via nav args (nullable).
 * @param page             The [SettingsPage] to search for the target key index.
 * @param listState        The lazy list state used to animate the scroll.
 * @param headerItemCount  Number of non-group items before the first group in the LazyColumn.
 * @param keyResolver      Resolves a key name string to a [SettingsKey]. Apps should pass
 *                         their own lookup function (e.g. `SettingsKeys::valueOfOrNull`).
 * @param topAppBarState   Optional [TopAppBarState] from the screen's [SettingsScaffold].
 *                         When provided, the top bar is instantly collapsed before the
 *                         animated scroll so the bar and the list scroll position stay
 *                         in sync and no secondary correction scroll occurs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberHighlightState(
    highlightKeyName: String?,
    page: SettingsPage,
    listState: LazyListState,
    headerItemCount: Int = 0,
    keyResolver: (String) -> SettingsKey<*>? = { null },
    topAppBarState: TopAppBarState? = null,
): SettingsKey<*>? {
    var highlightedKey by rememberSaveable { mutableStateOf(highlightKeyName) }

    // Persists for the lifetime of this BackStackEntry (resets when the entry is popped).
    // Prevents a second scroll when Compose Navigation restores this screen after
    // a forward navigation (e.g. user navigates to a sub-screen and comes back).
    var hasScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        val targetKey = keyResolver(highlightKeyName) ?: return@LaunchedEffect

        if (!hasScrolled) {
            // Fresh entry — scroll to the item and let the card's finite Animatable
            // handle the blink visually. No timer needed to clear highlightedKey
            // because the blink ends on its own without changing resolvedGroups.
            val lazyIndex = page.lazyListIndexOf(targetKey, headerItemCount)
            if (lazyIndex >= 0) {
                delay(400) // let the screen enter-transition finish

                // Snap the LargeTopAppBar to fully collapsed BEFORE the list scroll.
                // animateScrollToItem bypasses the nested scroll connection, so the bar
                // would stay expanded and later cause a correction scroll on recomposition.
                topAppBarState?.let { state ->
                    state.heightOffset = state.heightOffsetLimit
                }

                listState.animateScrollToItem(index = lazyIndex)
            }
            hasScrolled = true
        } else {
            // Restoration case: composable was recreated while highlight was still active
            // (e.g. config change or Compose disposing the back-stack entry mid-blink).
            // Clear immediately so the blink doesn't restart indefinitely on a fresh card.
            highlightedKey = null
        }
    }

    return highlightedKey?.let { keyResolver(it) }
}
