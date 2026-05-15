package `in`.hridayan.ashell.settings.presentation.page.search

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import kotlinx.coroutines.delay

/**
 * Manages scroll-to-item + blink-highlight state for a settings sub-screen.
 *
 * Call this once in each sub-screen that supports deep-link highlighting.
 * It returns the currently highlighted [SettingsKeys] (or null) — pass it to
 * [PreferenceItemView] to drive the card color blink.
 */
@Composable
fun rememberHighlightState(
    highlightKeyName: String?,
    settings: List<PreferenceGroup>,
    listState: LazyListState,
    headerItemCount: Int = 0,
): SettingsKeys? {
    var highlightedKey by rememberSaveable { mutableStateOf(highlightKeyName) }

    LaunchedEffect(highlightKeyName) {
        if (highlightKeyName == null) return@LaunchedEffect
        val targetKey = runCatching { SettingsKeys.valueOf(highlightKeyName) }.getOrNull()
            ?: return@LaunchedEffect

        // Find which group (= LazyColumn item) contains the target key
        val groupIndex = settings.indexOfFirst { group ->
            val items = when (group) {
                is PreferenceGroup.Items -> group.items
                is PreferenceGroup.Category -> group.items
                else -> emptyList()
            }
            items.any { it.key == targetKey }
        }
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

/**
 * Returns animated [CardColors] that blink between `surfaceContainer` and
 * `surfaceContainerHighest` when [isHighlighted] is true — matching stock
 * Android Settings highlight behavior.
 */
@Composable
fun highlightCardColors(isHighlighted: Boolean): CardColors {
    val normal = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest

    if (!isHighlighted) {
        return CardDefaults.cardColors(
            containerColor = normal,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    val transition = rememberInfiniteTransition(label = "highlight_blink")
    val fraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blink_fraction",
    )

    val animatedColor = lerp(normal, highlight, fraction)

    return CardDefaults.cardColors(
        containerColor = animatedColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}
