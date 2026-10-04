package `in`.hridayan.ashell.mirror.presentation.screens

import android.content.pm.ActivityInfo
import android.os.Build
import android.view.Display
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

/**
 * Hides this phone's status and navigation bars while [enabled], in the standard immersive mode:
 * an edge swipe reveals them briefly and the back gesture keeps working.
 *
 * The bars are shown again whenever fullscreen ends or the mirror leaves composition by any route,
 * so no other screen can inherit immersive mode. A configuration change is the one exception: the
 * activity is recreated straight into the same fullscreen mirror.
 */
@Composable
internal fun ImmersiveSystemBars(enabled: Boolean) {
    val activity = LocalActivity.current ?: return
    val view = LocalView.current
    DisposableEffect(enabled, activity, view) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        if (enabled) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (enabled && !activity.isChangingConfigurations) {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

/**
 * Asks this phone's display to run at a rate that shows every frame of an [fps] stream, below
 * Android 11. From Android 11 the video surface asks for it itself, which also works when the
 * display switches rate without changing mode.
 *
 * Only modes at the current resolution are considered, and the window's previous preference comes
 * back when the mirror leaves.
 */
@Suppress("DEPRECATION")
@Composable
internal fun PreferRefreshRate(fps: Int?) {
    val activity = LocalActivity.current ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return
    DisposableEffect(activity, fps) {
        val window = activity.window
        val previous = window.attributes.preferredDisplayModeId
        val modeId = fps?.let { activity.windowManager.defaultDisplay.modeIdFor(it) }
        if (modeId != null) window.attributes = window.attributes.apply { preferredDisplayModeId = modeId }
        onDispose {
            if (modeId != null) window.attributes = window.attributes.apply { preferredDisplayModeId = previous }
        }
    }
}

/** The slowest mode at the current resolution that keeps up with [fps], or the fastest one there is. */
private fun Display.modeIdFor(fps: Int): Int? {
    val current = mode
    val sameSize = supportedModes.filter {
        it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight
    }
    val fastEnough = sameSize.filter { it.refreshRate >= fps }.minByOrNull { it.refreshRate }
    return (fastEnough ?: sameSize.maxByOrNull { it.refreshRate })?.modeId
}

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
