package `in`.hridayan.ashell.logcat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity

class LogcatNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "logcat_channel"
        const val NOTIFICATION_ID = 3001
        const val ACTION_STOP = "in.hridayan.ashell.ACTION_STOP_LOGCAT"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.logcat_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.logcat_notification_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(): Notification {
        // Tapping the notification opens MainActivity with ACTION_OPEN_LOGCAT,
        // which MainActivity forwards to LogcatDeeplinkHolder → NavGraph navigates to LogcatScreen
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action =
                `in`.hridayan.ashell.logcat.data.session.LogcatDeeplinkHolder.ACTION_OPEN_LOGCAT
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, LogcatService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.logcat_is_running))
            .setContentText(context.getString(R.string.logcat_running_description))
            .setSmallIcon(R.drawable.ic_bug)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.stop),
                stopPendingIntent
            )
            .build()
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
