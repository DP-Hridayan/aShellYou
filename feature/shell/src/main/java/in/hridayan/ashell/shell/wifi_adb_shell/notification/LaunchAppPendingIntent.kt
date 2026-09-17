package `in`.hridayan.ashell.shell.wifi_adb_shell.notification

import android.app.PendingIntent
import android.content.Context

/**
 * A pending intent that opens the app, or null when the launcher entry cannot be resolved.
 *
 * Resolving through the package manager replaces a lookup by class name, which was evaluated inside
 * `onStartCommand` before the service reached the foreground. A failure there would have surfaced as
 * `ForegroundServiceDidNotStartInTimeException` with nothing pointing at the real cause. A
 * notification without a tap target is a far better outcome than a crash.
 */
internal fun Context.launchAppPendingIntent(requestCode: Int): PendingIntent? {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
    return PendingIntent.getActivity(
        this,
        requestCode,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
