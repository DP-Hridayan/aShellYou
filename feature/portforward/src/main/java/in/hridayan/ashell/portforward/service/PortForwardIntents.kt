package `in`.hridayan.ashell.portforward.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import `in`.hridayan.ashell.core.navigation.AppDeeplinkHolder

/** The intents that port-forwarding notifications launch. */
internal object PortForwardIntents {

    const val ACTION_STOP = "in.hridayan.ashell.portforward.ACTION_STOP"
    const val ACTION_STOP_ALL = "in.hridayan.ashell.portforward.ACTION_STOP_ALL"
    const val EXTRA_TUNNEL_ID = "tunnel_id"

    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    private const val OPEN_PORTS_REQUEST = 0
    private const val STOP_ALL_REQUEST = 1
    private const val STOP_REQUEST_BASE = 1_000
    private const val BROWSER_REQUEST_BASE = 2_000

    fun openPorts(context: Context): PendingIntent {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            action = AppDeeplinkHolder.ACTION_OPEN_PORTS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent()
        return PendingIntent.getActivity(context, OPEN_PORTS_REQUEST, launch, FLAGS)
    }

    fun stopAll(context: Context): PendingIntent = PendingIntent.getService(
        context,
        STOP_ALL_REQUEST,
        Intent(context, PortForwardService::class.java).setAction(ACTION_STOP_ALL),
        FLAGS
    )

    fun stop(context: Context, tunnelId: String): PendingIntent = PendingIntent.getService(
        context,
        STOP_REQUEST_BASE + tunnelId.hashCode(),
        Intent(context, PortForwardService::class.java)
            .setAction(ACTION_STOP)
            .putExtra(EXTRA_TUNNEL_ID, tunnelId),
        FLAGS
    )

    fun openInBrowser(context: Context, url: String): PendingIntent = PendingIntent.getActivity(
        context,
        BROWSER_REQUEST_BASE + url.hashCode(),
        Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        FLAGS
    )
}
