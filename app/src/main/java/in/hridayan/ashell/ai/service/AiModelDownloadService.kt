package `in`.hridayan.ashell.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps the app process alive during AI model downloads
 * and shows download progress in the notification shade.
 *
 * The actual download work is done in [AiModelRepository].
 * This service simply observes the repository's progress and reflects it
 * in a persistent notification.
 */
@AndroidEntryPoint
class AiModelDownloadService : Service() {

    companion object {
        private const val TAG = "AiDownloadSvc"
        private const val CHANNEL_ID = "ai_model_download"
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_CANCEL_ALL = "in.hridayan.ashell.ai.CANCEL_ALL_DOWNLOADS"

        fun start(context: Context) {
            Log.d(TAG, "Starting download service")
            val intent = Intent(context, AiModelDownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            Log.d(TAG, "Stopping download service")
            val intent = Intent(context, AiModelDownloadService::class.java)
            context.stopService(intent)
        }
    }

    @Inject
    lateinit var modelRepository: AiModelRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Preparing download...", -1))
        startObservingProgress()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL_ALL) {
            Log.d(TAG, "Cancel all downloads requested from notification")
            cancelAllDownloads()
            return START_NOT_STICKY
        }
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    private fun cancelAllDownloads() {
        val activeIds = modelRepository.observeDownloadProgress().value.keys.toList()
        for (modelId in activeIds) {
            modelRepository.cancelDownload(modelId)
        }
        stopSelf()
    }

    private fun buildCancelPendingIntent(): PendingIntent {
        val cancelIntent = Intent(this, AiModelDownloadService::class.java).apply {
            action = ACTION_CANCEL_ALL
        }
        return PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        observerJob?.cancel()
        super.onDestroy()
    }

    private fun startObservingProgress() {
        observerJob = serviceScope.launch {
            modelRepository.observeDownloadProgress().collect { progressMap ->
                if (progressMap.isEmpty()) {
                    // No active downloads — stop service
                    Log.d(TAG, "No active downloads, stopping service")
                    stopSelf()
                    return@collect
                }

                val notification = buildProgressNotification(progressMap)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildProgressNotification(progressMap: Map<String, DownloadProgress>): Notification {
        val activeDownloads = progressMap.entries.filter { it.value is DownloadProgress.Downloading }

        if (activeDownloads.isEmpty()) {
            // All completed or failed
            val completed = progressMap.entries.filter { it.value is DownloadProgress.Completed }
            return if (completed.isNotEmpty()) {
                buildNotification("Download complete", 100)
            } else {
                buildNotification("Download finished", -1)
            }
        }

        if (activeDownloads.size == 1) {
            val (modelId, progress) = activeDownloads.first()
            val downloading = progress as DownloadProgress.Downloading
            val modelName = `in`.hridayan.ashell.ai.data.local.model.ModelRegistry.findById(modelId)?.name ?: modelId
            val percent = downloading.progressPercent
            val downloadedMB = downloading.bytesDownloaded / (1024.0 * 1024.0)
            val totalMB = downloading.totalBytes / (1024.0 * 1024.0)

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_adb)
                .setContentTitle("Downloading $modelName")
                .setContentText("%.1f / %.1f MB".format(downloadedMB, totalMB))
                .setProgress(100, percent, false)
                .setOngoing(true)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .addAction(R.drawable.ic_adb, "Cancel", buildCancelPendingIntent())
                .build()
        }

        // Multiple concurrent downloads
        val totalBytes = activeDownloads.sumOf { (it.value as DownloadProgress.Downloading).totalBytes }
        val downloadedBytes = activeDownloads.sumOf { (it.value as DownloadProgress.Downloading).bytesDownloaded }
        val overallPercent = if (totalBytes > 0) ((downloadedBytes.toFloat() / totalBytes) * 100).toInt() else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle("Downloading ${activeDownloads.size} AI models")
            .setContentText("%.1f / %.1f MB".format(
                downloadedBytes / (1024.0 * 1024.0),
                totalBytes / (1024.0 * 1024.0)
            ))
            .setProgress(100, overallPercent, false)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.ic_adb, "Cancel All", buildCancelPendingIntent())
            .build()
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle("AI Model Download")
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress when downloading AI models"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
