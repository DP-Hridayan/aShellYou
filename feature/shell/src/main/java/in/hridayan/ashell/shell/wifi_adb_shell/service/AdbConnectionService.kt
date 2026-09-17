package `in`.hridayan.ashell.shell.wifi_adb_shell.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.notification.AdbConnectionNotificationHelper

/**
 * Foreground service to keep WiFi ADB connection alive when app is in background.
 *
 * Android aggressively kills background processes, which closes ADB TCP sockets.
 * This service runs in foreground with a notification to prevent the OS from
 * killing the connection. The heartbeat in WifiAdbRepository handles connection
 * status monitoring separately.
 */
class AdbConnectionService : Service() {

    companion object {
        private const val TAG = "AdbConnectionService"
        private const val NOTIFICATION_ID = 2002
        const val ACTION_DISCONNECT = "in.hridayan.ashell.ACTION_DISCONNECT"

        private val gate = ForegroundServiceGate()

        fun start(context: Context) {
            if (!gate.onStartRequested()) {
                Log.d(TAG, "Service already starting or running")
                return
            }
            Log.d(TAG, "Starting AdbConnectionService")
            context.startForegroundService(Intent(context, AdbConnectionService::class.java))
        }

        /**
         * A stop arriving while the start is still in flight is deferred rather than applied.
         *
         * Calling [Context.stopService] in that window destroys the service before it can call
         * `startForeground`, which Android punishes with
         * `ForegroundServiceDidNotStartInTimeException`.
         */
        fun stop(context: Context) {
            when (gate.onStopRequested()) {
                ServiceStopAction.StopService -> {
                    Log.d(TAG, "Stopping AdbConnectionService")
                    context.stopService(Intent(context, AdbConnectionService::class.java))
                }

                ServiceStopAction.AwaitStart ->
                    Log.d(TAG, "Stop deferred until the pending start lands")

                ServiceStopAction.Ignore ->
                    Log.d(TAG, "Nothing to stop")
            }
        }

        fun isServiceRunning(): Boolean = gate.isForegroundStarted
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AdbConnectionServiceEntryPoint {
        fun wifiAdbRepository(): WifiAdbRepository
    }

    private lateinit var notificationHelper: AdbConnectionNotificationHelper
    private lateinit var repository: WifiAdbRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        notificationHelper = AdbConnectionNotificationHelper(this)

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AdbConnectionServiceEntryPoint::class.java
        )
        repository = entryPoint.wifiAdbRepository()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")

        startForeground(NOTIFICATION_ID, notificationHelper.createNotification())

        if (gate.onForegroundStarted()) {
            Log.d(TAG, "Start was withdrawn while in flight - stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_DISCONNECT) {
            Log.d(TAG, "Disconnect action received - disconnecting")
            handleDisconnect()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        gate.onDestroyed()
        super.onDestroy()
    }

    private fun handleDisconnect() {
        repository.disconnect()

        WifiAdbConnection.setCurrentDevice(null)

        repository.stopHeartbeat()

        stopSelf()
    }
}
