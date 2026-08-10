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
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.logcat.data.emitter.LogcatEmitterFactory
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.usecase.ObserveLogsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the logcat process alive regardless of
 * whether the LogcatScreen is currently in the back-stack.
 *
 * The service reads [SettingsKeys.LogcatMode] at start time and uses
 * [LogcatEmitterFactory] to select the correct emitter (Basic/Shizuku/Root/Wireless).
 * Each parsed [LogEntry] is emitted into [LogcatSessionHolder], which the
 * ViewModel subscribes to for real-time display and background buffering.
 */
class LogcatService : Service() {

    companion object {
        private const val TAG = "LogcatService"

        @Volatile
        private var isRunning = false

        fun start(context: Context) {
            if (isRunning) return
            Log.d(TAG, "Starting LogcatService")
            context.startForegroundService(Intent(context, LogcatService::class.java))
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
        fun logcatEmitterFactory(): LogcatEmitterFactory
        fun settingsRepository(): SettingsRepository
    }

    private lateinit var notificationHelper: LogcatNotificationHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        notificationHelper = LogcatNotificationHelper(this)
        val ep = entryPoint()
        ep.logcatSessionHolder().setRunning(true)
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

        val ep = entryPoint()
        val observeLogs = ep.observeLogsUseCase()
        val sessionHolder = ep.logcatSessionHolder()
        val factory = ep.logcatEmitterFactory()
        val settingsRepo = ep.settingsRepository()

        serviceScope.launch {
            val mode = settingsRepo.getInt(SettingsKeys.LogcatMode).first()
            val emitter = factory.forMode(mode)
            Log.d(TAG, "Using emitter for logcat mode=$mode: ${emitter::class.simpleName}")
            observeLogs(emitter).collect { entry ->
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
        entryPoint().logcatSessionHolder().setRunning(false)
        Log.d(TAG, "LogcatService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun entryPoint() = EntryPointAccessors.fromApplication(
        applicationContext,
        LogcatServiceEntryPoint::class.java
    )
}
