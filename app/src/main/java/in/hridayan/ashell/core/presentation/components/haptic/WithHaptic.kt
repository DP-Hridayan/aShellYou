package `in`.hridayan.ashell.core.presentation.components.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.settings.data.SettingsKeys

@Composable
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val latestBlock = rememberUpdatedState(block)

    return retain(type, haptic, isHapticEnabled) {
        {
            if (isHapticEnabled) haptic.performHapticFeedback(type)
            latestBlock.value()
        }
    }
}