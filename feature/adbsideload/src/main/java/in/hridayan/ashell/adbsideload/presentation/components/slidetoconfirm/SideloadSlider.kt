package `in`.hridayan.ashell.adbsideload.presentation.components.slidetoconfirm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.slidetoconfirm.SlideToConfirm
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadSlider(
    enabled: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(confirmed) {
        if (confirmed) onConfirm()
    }

    SlideToConfirm(
        modifier = modifier,
        confirmed = confirmed,
        enabled = enabled,
        onConfirm = withHaptic(HapticFeedbackType.GestureThresholdActivate) {
            confirmed = true
        },
        initialText = stringResource(R.string.slide_to_sideload),
        finalText = stringResource(R.string.sideloading),
    )
}
