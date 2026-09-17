package `in`.hridayan.ashell.adbsideload.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.notification.SideloadProgressThrottle
import `in`.hridayan.ashell.adbsideload.domain.notification.toNotificationUpdate
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Keeps the process alive while a package is streaming, and shows its progress.
 *
 * The service does not perform the transfer. That belongs to the repository, which owns the USB
 * connection; this exists only so the process is not treated as cached, where Android would freeze
 * its threads and could reclaim it outright.
 */
@AndroidEntryPoint
class SideloadService : Service() {

    @Inject
    lateinit var repository: SideloadRepository

    private lateinit var notifications: SideloadNotificationHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val throttle = SideloadProgressThrottle()

    private var observeJob: Job? = null
    private var sawActiveTransfer = false

    override fun onCreate() {
        super.onCreate()
        notifications = SideloadNotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val operation = repository.operation.value
        ServiceCompat.startForeground(
            this,
            SideloadNotificationHelper.NOTIFICATION_ID,
            notifications.build(operation, operation.toNotificationUpdate()),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
        observeTransfer()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        observeJob?.cancel()
        serviceScope.cancel()
        notifications.cancel()
        super.onDestroy()
    }

    private fun observeTransfer() {
        if (observeJob?.isActive == true) return
        observeJob = serviceScope.launch {
            repository.operation.collect(::onOperationChanged)
        }
    }

    private fun onOperationChanged(operation: SideloadOperation) {
        if (operation.status.isActive) {
            sawActiveTransfer = true
            val update = operation.toNotificationUpdate()
            if (throttle.shouldPublish(update)) notifications.update(operation, update)
            return
        }
        if (sawActiveTransfer || operation.status.isFinished) {
            Log.d(TAG, "Transfer ended with ${operation.status}; stopping")
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "SideloadService"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, SideloadService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SideloadService::class.java))
        }
    }
}
