package `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.self.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity

/**
 * Helper class for managing notifications during own device pairing flow.
 * Handles all notification creation and display logic.
 */
class SelfPairingNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "own_device_pairing_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_SUBMIT_PAIRING_CODE = "in.hridayan.ashell.ACTION_SUBMIT_PAIRING_CODE"
        const val ACTION_CANCEL = "in.hridayan.ashell.ACTION_CANCEL"
        const val KEY_PAIRING_CODE = "pairing_code"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.self_pair_searching),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.self_pair_searching_hint)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showSearchingNotification(serviceClass: Class<*>): Notification {
        val notification = createSearchingNotification(serviceClass)
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

    fun showEnterCodeNotification(serviceClass: Class<*>) {
        notificationManager.notify(NOTIFICATION_ID, createEnterCodeNotification(serviceClass))
    }

    fun showPairingInProgressNotification() {
        notificationManager.notify(NOTIFICATION_ID, createPairingNotification())
    }

    fun showSuccessNotification() {
        notificationManager.notify(NOTIFICATION_ID, createSuccessNotification())
    }

    fun showFailureNotification(message: String) {
        notificationManager.notify(NOTIFICATION_ID, createFailureNotification(message))
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createSearchingNotification(serviceClass: Class<*>): Notification {
        val cancelIntent = Intent(context, serviceClass).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            context, 0, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.self_pair_searching))
            .setContentText(context.getString(R.string.self_pair_searching_hint))
            .setSmallIcon(R.drawable.ic_wireless)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_cancel, context.getString(R.string.cancel), cancelPendingIntent)
            .build()
    }

    private fun createEnterCodeNotification(serviceClass: Class<*>): Notification {
        val remoteInput = RemoteInput.Builder(KEY_PAIRING_CODE)
            .setLabel(context.getString(R.string.self_pair_enter_code_hint))
            .build()

        val submitIntent = Intent(context, serviceClass).apply {
            action = ACTION_SUBMIT_PAIRING_CODE
        }
        val submitPendingIntent = PendingIntent.getService(
            context, 1, submitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val cancelIntent = Intent(context, serviceClass).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            context, 2, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val submitAction = NotificationCompat.Action.Builder(
            R.drawable.ic_check, context.getString(R.string.pair), submitPendingIntent
        ).addRemoteInput(remoteInput).build()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.self_pair_found))
            .setContentText(context.getString(R.string.self_pair_enter_code))
            .setSmallIcon(R.drawable.ic_wireless)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(submitAction)
            .addAction(R.drawable.ic_cancel, context.getString(R.string.cancel), cancelPendingIntent)
            .build()
    }

    private fun createPairingNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.self_pair_in_progress))
            .setContentText(context.getString(R.string.self_pair_in_progress_hint))
            .setSmallIcon(R.drawable.ic_wireless)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createSuccessNotification(): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 3, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.self_pair_success))
            .setContentText(context.getString(R.string.self_pair_success_hint))
            .setSmallIcon(R.drawable.ic_check)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()
    }

    private fun createFailureNotification(message: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.self_pair_failed))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_error)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
    }
}
