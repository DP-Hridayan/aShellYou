package `in`.hridayan.ashell.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

fun isNotificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        return hasPermission && NotificationManagerCompat.from(context).areNotificationsEnabled()
    } else {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun createAppNotificationSettingsIntent(context: Context): Intent {
    return Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
}