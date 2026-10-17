package `in`.hridayan.ashell.portforward.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.service.ForegroundServiceGate
import `in`.hridayan.ashell.core.common.service.ServiceStopAction
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.notification.PortForwardNotificationContent
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Keeps the process alive while tunnels run, and shows their status.
 *
 * The service owns no tunnel state. [TunnelController] does, and starts and stops this service as
 * tunnels come and go; the service only renders the notification and forwards its actions.
 */
@AndroidEntryPoint
class PortForwardService : Service() {

    @Inject
    lateinit var controller: TunnelController

    @Inject
    lateinit var settings: PortForwardSettingsSource

    private lateinit var notifications: PortForwardNotificationBuilder
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notifications = PortForwardNotificationBuilder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            PortForwardNotificationBuilder.NOTIFICATION_ID,
            notifications.build(currentContent()),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
        if (gate.onForegroundStarted()) {
            Log.d(TAG, "Stop arrived while starting; stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        handleAction(intent)
        observeTunnels()
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceScope.launch {
            if (settings.current().stopOnTaskRemoved) controller.stopAll()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        gate.onDestroyed()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handleAction(intent: Intent?) {
        when (intent?.action) {
            PortForwardIntents.ACTION_STOP_ALL -> serviceScope.launch { controller.stopAll() }
            PortForwardIntents.ACTION_STOP -> intent.getStringExtra(PortForwardIntents.EXTRA_TUNNEL_ID)
                ?.let { id -> serviceScope.launch { controller.stop(id) } }
        }
    }

    private fun observeTunnels() {
        if (observeJob?.isActive == true) return
        observeJob = serviceScope.launch {
            controller.runtimes
                .map { PortForwardNotificationContent.from(it.values) }
                .distinctUntilChanged()
                .conflate()
                .collect { content ->
                    if (content == null) {
                        stopSelf()
                        return@collect
                    }
                    notifications.update(content)
                    delay(MIN_UPDATE_INTERVAL_MS)
                }
        }
    }

    private fun currentContent(): PortForwardNotificationContent? =
        PortForwardNotificationContent.from(controller.runtimes.value.values)

    companion object {
        private const val TAG = "PortForwardService"
        private const val MIN_UPDATE_INTERVAL_MS = 1_000L

        private val gate = ForegroundServiceGate()

        fun start(context: Context) {
            if (!gate.onStartRequested()) return
            context.startForegroundService(Intent(context, PortForwardService::class.java))
        }

        /** A stop arriving while the start is still in flight is applied once the start lands. */
        fun stop(context: Context) {
            when (gate.onStopRequested()) {
                ServiceStopAction.StopService ->
                    context.stopService(Intent(context, PortForwardService::class.java))

                ServiceStopAction.AwaitStart -> Log.d(TAG, "Stop deferred until the start lands")
                ServiceStopAction.Ignore -> Unit
            }
        }
    }
}
