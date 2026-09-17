package `in`.hridayan.ashell.adbsideload.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.notification.SideloadNotificationUpdate
import `in`.hridayan.ashell.core.resources.R

/**
 * Builds the ongoing notification shown while a package is streaming.
 *
 * It carries no cancel action on purpose. Recovery reads the package more than once, so blocks are
 * still being served while it verifies and installs, and a single tap from the notification shade
 * should not be able to interrupt an installation in progress. Tapping opens the screen, where
 * cancelling is deliberate.
 */
class SideloadNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    fun build(operation: SideloadOperation, update: SideloadNotificationUpdate): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title(operation))
            .setContentText(statusText(operation))
            .setSmallIcon(R.drawable.ic_otg)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(MAX_PROGRESS, update.percent, update.isIndeterminate)
            .apply { openAppIntent()?.let { setContentIntent(it) } }
            .build()

    fun update(operation: SideloadOperation, update: SideloadNotificationUpdate) {
        notificationManager.notify(NOTIFICATION_ID, build(operation, update))
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun title(operation: SideloadOperation): String = operation.fileName
        .takeIf { it.isNotBlank() }
        ?: context.getString(R.string.adb_sideload)

    private fun statusText(operation: SideloadOperation): String = when (operation.status) {
        SideloadStatus.READING_FILE -> context.getString(R.string.preparing_file)
        SideloadStatus.WAITING_FOR_RECOVERY -> context.getString(R.string.waiting_for_recovery)
        SideloadStatus.COMPLETE -> context.getString(R.string.sideload_complete)
        SideloadStatus.CANCELLED -> context.getString(R.string.cancelled)
        SideloadStatus.ERROR -> context.getString(R.string.error)
        else -> context.getString(R.string.sideloading)
    }

    private fun openAppIntent(): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        return PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.sideload_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.sideload_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 2004
        private const val CHANNEL_ID = "adb_sideload_channel"
        private const val OPEN_APP_REQUEST_CODE = 0
        private const val MAX_PROGRESS = 100
    }
}
