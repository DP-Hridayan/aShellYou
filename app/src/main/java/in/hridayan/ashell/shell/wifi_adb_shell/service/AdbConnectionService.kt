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
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.notification.AdbConnectionNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service to keep WiFi ADB connection alive when app is in background.
 * 
 * Android aggressively kills background processes, which closes ADB TCP sockets.
 * This service runs in foreground with a notification to prevent the OS from
 * killing the connection.
 */
class AdbConnectionService : Service() {

    companion object {
        private const val TAG = "AdbConnectionService"
        private const val NOTIFICATION_ID = 2002
        
        @Volatile
        private var isRunning = false
        
        fun start(context: Context) {
            if (isRunning) {
                Log.d(TAG, "Service already running")
                return
            }
            Log.d(TAG, "Starting AdbConnectionService")
            val intent = Intent(context, AdbConnectionService::class.java)
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            Log.d(TAG, "Stopping AdbConnectionService")
            context.stopService(Intent(context, AdbConnectionService::class.java))
        }
        
        fun isServiceRunning(): Boolean = isRunning
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AdbConnectionServiceEntryPoint {
        fun wifiAdbRepository(): WifiAdbRepository
    }

    private lateinit var repository: WifiAdbRepository
    private lateinit var notificationHelper: AdbConnectionNotificationHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var keepAliveJob: Job? = null

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
        Log.d(TAG, "onStartCommand")
        isRunning = true

        startForeground(NOTIFICATION_ID, notificationHelper.createNotification())

        startKeepAlive()
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        isRunning = false
        keepAliveJob?.cancel()
        keepAliveJob = null
        super.onDestroy()
    }

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = serviceScope.launch {
            Log.d(TAG, "Keep-alive loop started")
            while (isRunning) {
                try {
                    delay(10_000) // Every 10 seconds
                    
                    val manager = AdbConnectionManager.getInstance(applicationContext)
                    if (manager.isConnected) {
                        // Send ping to keep socket alive
                        try {
                            val stream = manager.openStream("shell:echo keep_alive")
                            val response = stream.openInputStream().bufferedReader().readText()
                            stream.close()
                            Log.d(TAG, "Keep-alive ping: ${response.trim()}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Keep-alive ping failed: ${e.message}")
                        }
                    } else {
                        Log.d(TAG, "ADB not connected, stopping service")
                        stopSelf()
                        break
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Keep-alive error", e)
                }
            }
        }
    }
}
