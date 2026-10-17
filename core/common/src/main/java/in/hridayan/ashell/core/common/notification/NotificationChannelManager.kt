package `in`.hridayan.ashell.core.common.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import `in`.hridayan.ashell.core.resources.R

/**
 * Manager responsible for initializing and categorizing all Android notification channels
 * and channel groups centrally on application startup.
 *
 * Registering channels centrally in the [android.app.Application.onCreate] is the officially
 * recommended approach by Android. It ensures that all notification categories are immediately
 * visible to the user in the System Settings, even before they interact with specific features.
 */
object NotificationChannelManager {
    const val CHANNEL_PAIRING = "own_device_pairing_channel"
    const val CHANNEL_ADB_CONNECTION = "adb_connection_channel"
    const val CHANNEL_SIDELOAD = "adb_sideload_channel"
    const val CHANNEL_BACKUP = "auto_backup_channel"
    const val CHANNEL_LOGCAT = "logcat_channel"
    const val CHANNEL_TILE_ERRORS = "tile_execution_errors"
    const val CHANNEL_PORT_FORWARDING = "port_forwarding"
    const val CHANNEL_PORT_FORWARDING_ALERTS = "port_forwarding_alerts"

    private const val GROUP_CONNECTION = "connection_group"
    private const val GROUP_TASKS = "tasks_group"
    private const val GROUP_DEVELOPER = "developer_group"
    private const val GROUP_QS = "qs_group"

    /**
     * Creates all notification channel groups and their respective channels.
     *
     * @param context The application context used to retrieve string resources and the notification manager.
     */
    fun initialize(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val connectionGroup = NotificationChannelGroup(
            GROUP_CONNECTION,
            context.getString(R.string.notification_group_connection)
        )
        val tasksGroup = NotificationChannelGroup(
            GROUP_TASKS,
            context.getString(R.string.notification_group_tasks)
        )
        val developerGroup = NotificationChannelGroup(
            GROUP_DEVELOPER,
            context.getString(R.string.notification_group_developer)
        )
        val qsGroup = NotificationChannelGroup(
            GROUP_QS,
            context.getString(R.string.notification_group_qs)
        )

        notificationManager.createNotificationChannelGroups(
            listOf(connectionGroup, tasksGroup, developerGroup, qsGroup)
        )

        val pairingChannel = NotificationChannel(
            CHANNEL_PAIRING,
            context.getString(R.string.self_pair_searching),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.self_pair_searching_hint)
            setShowBadge(true)
            group = GROUP_CONNECTION
        }

        val adbConnectionChannel = NotificationChannel(
            CHANNEL_ADB_CONNECTION,
            context.getString(R.string.adb_connection_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.adb_connection_channel_description)
            setShowBadge(false)
            group = GROUP_CONNECTION
        }

        val sideloadChannel = NotificationChannel(
            CHANNEL_SIDELOAD,
            context.getString(R.string.sideload_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.sideload_channel_description)
            setShowBadge(false)
            group = GROUP_TASKS
        }

        val backupChannel = NotificationChannel(
            CHANNEL_BACKUP,
            context.getString(R.string.auto_backup),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            group = GROUP_TASKS
        }

        val logcatChannel = NotificationChannel(
            CHANNEL_LOGCAT,
            context.getString(R.string.logcat_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.logcat_notification_channel_description)
            setShowBadge(false)
            group = GROUP_DEVELOPER
        }

        val tileChannel = NotificationChannel(
            CHANNEL_TILE_ERRORS,
            context.getString(R.string.tile_execution_errors),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.tile_execution_errors_channel_description)
            setShowBadge(true)
            group = GROUP_QS
        }

        notificationManager.createNotificationChannels(
            listOf(
                pairingChannel,
                adbConnectionChannel,
                sideloadChannel,
                backupChannel,
                logcatChannel,
                tileChannel
            ) + portForwardingChannels(context)
        )
    }

    private fun portForwardingChannels(context: Context): List<NotificationChannel> = listOf(
        NotificationChannel(
            CHANNEL_PORT_FORWARDING,
            context.getString(R.string.port_forwarding),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.port_forwarding_channel_description)
            setShowBadge(false)
            group = GROUP_CONNECTION
        },
        NotificationChannel(
            CHANNEL_PORT_FORWARDING_ALERTS,
            context.getString(R.string.port_forwarding_alerts),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.port_forwarding_alerts_channel_description)
            setShowBadge(true)
            group = GROUP_CONNECTION
        }
    )
}
