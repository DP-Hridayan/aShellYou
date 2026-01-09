package `in`.hridayan.ashell.shell.wifi_adb_shell.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity
import `in`.hridayan.ashell.shell.wifi_adb_shell.service.AdbConnectionService

/**
 * Helper class for managing notifications for ADB connection foreground service.
 */
class AdbConnectionNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "adb_connection_channel"
        const val ACTION_DISCONNECT = "in.hridayan.ashell.ACTION_DISCONNECT"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.adb_connection_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.adb_connection_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(context, AdbConnectionService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            context, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.adb_connection_active))
            .setContentText(context.getString(R.string.adb_connection_description))
            .setSmallIcon(R.drawable.ic_wireless)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_cancel, context.getString(R.string.disconnect), disconnectPendingIntent)
            .build()
    }

    fun cancel() {
        notificationManager.cancelAll()
    }
}
