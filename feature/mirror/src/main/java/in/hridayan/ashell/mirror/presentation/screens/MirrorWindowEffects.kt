package `in`.hridayan.ashell.mirror.presentation.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

@Composable
internal fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/**
 * Rotates this phone to match the mirrored screen, like a video player going fullscreen.
 *
 * Changing the requested orientation recreates the activity, which disposes and re-enters this
 * effect. The original orientation is therefore saved across recreation and only restored when the
 * screen is really left, otherwise restoring it would trigger another recreation in a loop.
 */
@Composable
internal fun FollowVideoOrientation(videoSize: VideoSize?) {
    val activity = LocalActivity.current ?: return
    val originalOrientation = rememberSaveable { activity.requestedOrientation }

    LaunchedEffect(videoSize?.isLandscape) {
        val isLandscape = videoSize?.isLandscape ?: return@LaunchedEffect
        activity.requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        }
    }

    DisposableEffect(activity) {
        onDispose {
            if (!activity.isChangingConfigurations) activity.requestedOrientation = originalOrientation
        }
    }
}
