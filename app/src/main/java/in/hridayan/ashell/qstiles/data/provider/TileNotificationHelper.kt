package `in`.hridayan.ashell.qstiles.data.provider

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileNotificationHelper @Inject constructor(
    private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "tile_execution_errors"
        const val GROUP_KEY = "tile_execution_errors_group"
        const val SUMMARY_NOTIFICATION_ID = 9999
    }

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tile Execution Errors",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for Quick Settings tile execution failures."
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Posts a failure notification for [tileName].
     * All failure notifications are grouped under [GROUP_KEY] to prevent spam.
     * Tapping opens [MainActivity] for the user to review logs.
     */
    fun notifyFailure(
        tileName: String,
        errorType: TileErrorType,
        errorMessage: String,
        notificationId: Int
    ) {
        val reason = when (errorType) {
            TileErrorType.PERMISSION_DENIED -> "Permission denied — check Shizuku or Root access."
            TileErrorType.TIMEOUT -> "Command timed out."
            TileErrorType.EXECUTION_FAILED -> "Command failed: $errorMessage"
            TileErrorType.UNKNOWN -> "An unknown error occurred."
            TileErrorType.NONE -> return // No failure — do nothing
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle("Tile \"$tileName\" failed")
            .setContentText(reason)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reason))
            .setGroup(GROUP_KEY)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
        postGroupSummary()
    }

    private fun postGroupSummary() {
        val summary = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle("Tile execution errors")
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summary)
    }
}
