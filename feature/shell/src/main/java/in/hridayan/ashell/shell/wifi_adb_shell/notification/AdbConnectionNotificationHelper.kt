package `in`.hridayan.ashell.shell.wifi_adb_shell.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.core.common.notification.NotificationChannelManager
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.service.AdbConnectionService

/**
 * Helper class for managing notifications for ADB connection foreground service.
 */
class AdbConnectionNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = NotificationChannelManager.CHANNEL_ADB_CONNECTION
        private const val OPEN_APP_REQUEST_CODE = 0
        const val ACTION_DISCONNECT = "in.hridayan.ashell.ACTION_DISCONNECT"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(): Notification {
        val openAppPendingIntent = context.launchAppPendingIntent(OPEN_APP_REQUEST_CODE)

        val disconnectIntent = Intent(context, AdbConnectionService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            context,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.adb_connection_active))
            .setContentText(context.getString(R.string.adb_connection_description))
            .setSmallIcon(R.drawable.ic_wireless)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply { openAppPendingIntent?.let { setContentIntent(it) } }
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.disconnect),
                disconnectPendingIntent
            )
            .build()
    }

    fun cancel() {
        notificationManager.cancelAll()
    }
}
