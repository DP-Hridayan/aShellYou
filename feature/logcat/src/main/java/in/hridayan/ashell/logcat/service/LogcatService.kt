package `in`.hridayan.ashell.logcat.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.usecase.ObserveLogsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the logcat process alive regardless of
 * whether the LogcatScreen is currently in the back-stack.
 *
 * The service emits each parsed [LogEntry] into [LogcatSessionHolder],
 * which the ViewModel subscribes to. This allows background buffering
 * to continue even when the user navigates away from the screen.
 */
class LogcatService : Service() {

    companion object {
        private const val TAG = "LogcatService"

        @Volatile
        private var isRunning = false

        fun start(context: Context) {
            if (isRunning) return
            Log.d(TAG, "Starting LogcatService")
            val intent = Intent(context, LogcatService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            Log.d(TAG, "Stopping LogcatService")
            context.stopService(Intent(context, LogcatService::class.java))
        }

        fun isServiceRunning(): Boolean = isRunning
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LogcatServiceEntryPoint {
        fun observeLogsUseCase(): ObserveLogsUseCase
        fun logcatSessionHolder(): LogcatSessionHolder
    }

    private lateinit var notificationHelper: LogcatNotificationHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        notificationHelper = LogcatNotificationHelper(this)
        // Update singleton state so all ViewModel instances see the change
        val entryPointForRunning = EntryPointAccessors.fromApplication(
            applicationContext, LogcatServiceEntryPoint::class.java
        )
        entryPointForRunning.logcatSessionHolder().setRunning(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == LogcatNotificationHelper.ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            LogcatNotificationHelper.NOTIFICATION_ID,
            notificationHelper.createNotification()
        )

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext, LogcatServiceEntryPoint::class.java
        )
        val observeLogs = entryPoint.observeLogsUseCase()
        val sessionHolder = entryPoint.logcatSessionHolder()

        serviceScope.launch {
            observeLogs().collect { entry ->
                sessionHolder.emit(entry)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        notificationHelper.cancel()
        // Update singleton state
        val entryPointForRunning = EntryPointAccessors.fromApplication(
            applicationContext, LogcatServiceEntryPoint::class.java
        )
        entryPointForRunning.logcatSessionHolder().setRunning(false)
        Log.d(TAG, "LogcatService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
