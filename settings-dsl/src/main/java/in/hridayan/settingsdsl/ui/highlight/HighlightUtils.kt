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
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.item.settingsContent
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Manages scroll-to-item + blink-highlight state for a DSL-based settings sub-screen.
 *
 * Returns the currently highlighted [SettingsKey] (or null) to pass as `highlightedKey`
 * to [resolveAll] / [settingsContent].
 *
 * @param highlightKeyName Key name from nav args (nullable — no-op when null).
 * @param page             The [SettingsPage] to search for the target item index.
 * @param listState        Lazy list state used for the scroll animation.
 * @param headerItemCount  Non-group items before the first group in the LazyColumn.
 * @param keyResolver      Resolves a key name string to a [SettingsKey].
 * @param topAppBarState   When provided, the top bar is pre-collapsed before scrolling
 *                         to keep it in sync with the programmatic scroll position.
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
    // Persists for the BackStackEntry lifetime — prevents re-scroll on forward nav + back.
    var hasScrolled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        val targetKey = keyResolver(highlightKeyName) ?: return@LaunchedEffect

        if (!hasScrolled) {
            val lazyIndex = page.lazyListIndexOf(targetKey, headerItemCount)
            if (lazyIndex >= 0) {
                delay(400.milliseconds)
                // Pre-collapse before scroll so the bar and list stay in sync.
                // animateScrollToItem bypasses NestedScrollConnection, which would
                // otherwise leave the bar expanded and cause a secondary correction scroll.
                topAppBarState?.let { it.heightOffset = it.heightOffsetLimit }
                listState.animateScrollToItem(index = lazyIndex)
            }
            hasScrolled = true
        } else {
            // Composable recreated mid-blink (e.g. config change) — clear stale highlight.
            highlightedKey = null
        }
    }

    return highlightedKey?.let { keyResolver(it) }
}
