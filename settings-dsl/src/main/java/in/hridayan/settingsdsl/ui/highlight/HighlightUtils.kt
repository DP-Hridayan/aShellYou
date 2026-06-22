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
import kotlin.time.Duration.Companion.milliseconds

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

    // Guards against re-scrolling when Compose Navigation restores this screen after
    // a forward navigation (e.g. user goes to a sub-screen and comes back).
    // rememberSaveable persists this for the lifetime of the BackStackEntry, so it
    // resets naturally when the entry is popped and the user navigates here via
    // search again (a fresh BackStackEntry with hasScrolled = false).
    var hasScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        if (hasScrolled) return@LaunchedEffect  // already scrolled for this nav entry

        val targetKey = keyResolver(highlightKeyName) ?: return@LaunchedEffect

        val lazyIndex = page.lazyListIndexOf(targetKey, headerItemCount)
        if (lazyIndex >= 0) {
            delay(400.milliseconds) // let the screen enter-transition finish

            // Snap the LargeTopAppBar to fully collapsed BEFORE the list scroll.
            // animateScrollToItem bypasses the nested scroll connection, so the bar
            // would stay expanded and later cause a correction scroll on recomposition.
            topAppBarState?.let { state ->
                state.heightOffset = state.heightOffsetLimit
            }

            listState.animateScrollToItem(index = lazyIndex)
        }

        hasScrolled = true  // mark done — survives forward nav + back via rememberSaveable

        // Clear highlight after the blink animation completes (~2s).
        // Runs in the same LaunchedEffect so it never restarts independently.
        delay(2400.milliseconds)
        highlightedKey = null
    }

    return highlightedKey?.let { keyResolver(it) }
}
