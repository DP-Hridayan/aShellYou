package `in`.hridayan.ashell.shell.wifi_adb_shell.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
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

    private lateinit var notificationHelper: AdbConnectionNotificationHelper

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        notificationHelper = AdbConnectionNotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        isRunning = true
        
        // Start foreground - this keeps the process alive
        startForeground(NOTIFICATION_ID, notificationHelper.createNotification())
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        isRunning = false
        super.onDestroy()
    }
}
