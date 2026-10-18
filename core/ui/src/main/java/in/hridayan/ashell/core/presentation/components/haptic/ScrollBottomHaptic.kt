package `in`.hridayan.ashell.core.presentation.components.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys

private const val SCROLL_THRESHOLD_PX = 1.0f

/**
 * A [NestedScrollConnection] that triggers haptic feedback when an active user scroll
 * action reaches the bottom boundary of a scrollable container.
 *
 * @param isEnabled Lambda providing the current enabled state from application settings.
 * @param onBottomReached Callback invoked once when scrolling reaches the bottom edge.
 * @param thresholdPx Minimum pixel delta required to distinguish intentional scroll from noise.
 */
class ScrollBottomHapticConnection(
    private val isEnabled: () -> Boolean,
    private val onBottomReached: () -> Unit,
    private val thresholdPx: Float = SCROLL_THRESHOLD_PX,
) : NestedScrollConnection {

    private var hasVibratedForCurrentBoundary = false
    private var isActivelyScrolling = false

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y > thresholdPx) {
            hasVibratedForCurrentBoundary = false
            isActivelyScrolling = false
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (!isEnabled()) return Offset.Zero

        if (consumed.y < -thresholdPx) {
            isActivelyScrolling = true
        }

        if (consumed.y > thresholdPx) {
            hasVibratedForCurrentBoundary = false
            isActivelyScrolling = false
        }

        if (source == NestedScrollSource.UserInput && available.y < -thresholdPx) {
            if (!hasVibratedForCurrentBoundary && isActivelyScrolling) {
                onBottomReached()
                hasVibratedForCurrentBoundary = true
            }
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (!isEnabled()) return Velocity.Zero

        if (available.y < -thresholdPx && !hasVibratedForCurrentBoundary && isActivelyScrolling) {
            onBottomReached()
            hasVibratedForCurrentBoundary = true
        }

        hasVibratedForCurrentBoundary = false
        isActivelyScrolling = false

        return Velocity.Zero
    }
}

/**
 * Remembers a [ScrollBottomHapticConnection] tied to the application haptics setting.
 *
 * @param feedbackType The [HapticFeedbackType] to perform when the bottom boundary is reached.
 */
@Composable
fun rememberScrollBottomHapticConnection(
    feedbackType: HapticFeedbackType = HapticFeedbackType.VirtualKey
): ScrollBottomHapticConnection {
    val isMasterHapticEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val isScrollBottomHapticEnabled = LocalSettings.current[SettingsKeys.ScrollBottomHaptic]
    val isEnabled = isMasterHapticEnabled && isScrollBottomHapticEnabled
    val hapticFeedback = LocalHapticFeedback.current

    return remember(isEnabled, hapticFeedback, feedbackType) {
        ScrollBottomHapticConnection(
            isEnabled = { isEnabled },
            onBottomReached = {
                hapticFeedback.performHapticFeedback(feedbackType)
            }
        )
    }
}

/**
 * Modifier that attaches scroll bottom haptic feedback to the receiver hierarchy.
 *
 * @param feedbackType The [HapticFeedbackType] to perform when the bottom boundary is reached.
 */
@Composable
fun Modifier.scrollBottomHaptic(
    feedbackType: HapticFeedbackType = HapticFeedbackType.VirtualKey
): Modifier {
    val connection = rememberScrollBottomHapticConnection(feedbackType)
    return this.nestedScroll(connection)
}
