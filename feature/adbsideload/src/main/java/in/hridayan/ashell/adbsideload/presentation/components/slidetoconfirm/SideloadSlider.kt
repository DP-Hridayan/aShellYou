package `in`.hridayan.ashell.adbsideload.presentation.components.slidetoconfirm

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.slidetoconfirm.SlideToConfirm
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadSlider(
    enabled: Boolean,
    confirmed: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SlideToConfirm(
        modifier = modifier,
        confirmed = confirmed,
        enabled = enabled,
        onConfirm = withHaptic(HapticFeedbackType.GestureThresholdActivate) { onConfirm() },
        initialText = stringResource(R.string.slide_to_sideload),
        finalText = stringResource(R.string.sideloading),
    )
}
