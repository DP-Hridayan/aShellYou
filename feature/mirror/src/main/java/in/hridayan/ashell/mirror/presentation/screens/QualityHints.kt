package `in`.hridayan.ashell.mirror.presentation.screens

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.presentation.model.QualityActions
import `in`.hridayan.ashell.mirror.presentation.model.QualityUiState

/**
 * The quality messages: an offer to lower quality when the video can't keep up, and a notice when
 * the device couldn't encode the chosen codec. Each is marked shown only after its snackbar closes,
 * because clearing the flag earlier would restart this effect and dismiss the snackbar at once.
 */
@Composable
internal fun QualityHints(state: QualityUiState, hostState: SnackbarHostState, actions: QualityActions) {
    val struggling = stringResource(R.string.video_struggling)
    val lowerQuality = stringResource(R.string.lower_quality)
    LaunchedEffect(state.isStruggleHintPending) {
        if (!state.isStruggleHintPending) return@LaunchedEffect
        val result = hostState.showSnackbar(struggling, actionLabel = lowerQuality, duration = SnackbarDuration.Long)
        actions.onStruggleHintShown()
        if (result == SnackbarResult.ActionPerformed) actions.onLowerQuality()
    }

    val fallback = state.codecFallback?.let { stringResource(R.string.codec_fallback, it.displayName) }
    LaunchedEffect(fallback) {
        if (fallback == null) return@LaunchedEffect
        hostState.showSnackbar(fallback)
        actions.onCodecFallbackShown()
    }
}
