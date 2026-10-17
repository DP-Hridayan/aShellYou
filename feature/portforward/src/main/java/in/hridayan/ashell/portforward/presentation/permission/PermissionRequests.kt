package `in`.hridayan.ashell.portforward.presentation.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.ashell.core.common.network.LocalNetworkPermission
import `in`.hridayan.ashell.core.utils.isNotificationPermissionGranted

/** @param onResult runs with the answer each time the system prompt closes. */
@Composable
fun rememberLocalNetworkAccess(onResult: (granted: Boolean) -> Unit = {}): LocalNetworkAccessState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var granted by remember { mutableStateOf(LocalNetworkPermission.isGranted(context)) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    OnResume { granted = LocalNetworkPermission.isGranted(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted = result
        permanentlyDenied = !result && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, LocalNetworkPermission.PERMISSION)
        onResult(result)
    }
    return LocalNetworkAccessState(
        granted = granted,
        permanentlyDenied = permanentlyDenied,
        request = { launcher.launch(LocalNetworkPermission.PERMISSION) },
        openSettings = { context.startActivity(appDetailsSettingsIntent(context)) }
    )
}

/**
 * Asks once for the notification permission before [onDone] runs, when Android 13 or later needs
 * it. [onDone] runs whatever the answer: tunnels work without notifications.
 */
@Composable
fun rememberNotificationPermissionRequest(onDone: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onDone() }
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onDone()
        }
    }
}

/** True while notifications are allowed, re-read whenever the screen resumes. */
@Composable
fun rememberNotificationsAllowed(): Boolean {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(isNotificationPermissionGranted(context)) }
    OnResume { allowed = isNotificationPermissionGranted(context) }
    return allowed
}

@Composable
private fun OnResume(action: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) action() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private fun appDetailsSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(PACKAGE_SCHEME, context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private const val PACKAGE_SCHEME = "package"
