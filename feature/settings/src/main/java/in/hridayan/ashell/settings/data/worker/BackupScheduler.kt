package `in`.hridayan.ashell.settings.data.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import `in`.hridayan.ashell.core.common.domain.model.BackupFrequency
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object BackupScheduler {

    const val WORK_NAME = "auto_backup_work"
    private const val ONE_SHOT_WORK_NAME = "auto_backup_now"

    fun schedule(context: Context, hour: Int, minute: Int, frequency: Int) {
        val now = LocalDateTime.now()
        var scheduledTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))

        // If the target time has already passed today, schedule for tomorrow
        if (scheduledTime.isBefore(now) || scheduledTime.isEqual(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduledTime).toMillis()
        val repeatIntervalDays = BackupFrequency.intervalDays(frequency)

        Log.i(TAG, "schedule() → target=$scheduledTime initialDelayMs=$initialDelay repeatDays=$repeatIntervalDays")

        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(repeatIntervalDays, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
        Log.i(TAG, "schedule() → enqueued with CANCEL_AND_REENQUEUE policy")
    }

    /** Enqueues a one-time immediate backup using the same [AutoBackupWorker]. */
    fun runNow(context: Context) {
        Log.i(TAG, "runNow() → enqueueing one-shot backup")
        val request = OneTimeWorkRequestBuilder<AutoBackupWorker>()
            .setInputData(workDataOf(AutoBackupWorker.KEY_MANUAL_TRIGGER to true))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancel(context: Context) {
        Log.w(TAG, "cancel() → cancelling scheduled backup work")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private const val TAG = "ABScheduler"
}
