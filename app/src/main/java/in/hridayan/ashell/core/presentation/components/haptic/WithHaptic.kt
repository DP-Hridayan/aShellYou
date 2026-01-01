package `in`.hridayan.ashell.core.presentation.components.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import `in`.hridayan.ashell.core.common.LocalSettings

@Composable
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current.isHapticEnabled

    return remember(haptic, isHapticEnabled, block) {
        {
            if (isHapticEnabled) haptic.performHapticFeedback(type)
            block()
        }
    }
}