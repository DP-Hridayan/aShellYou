package `in`.hridayan.ashell.settings.data.worker


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.common.domain.model.BackupType
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupAndRestoreRepository: BackupAndRestoreRepository,
    private val settingsRepository: SettingsRepository,
    private val googleAuthRepository: GoogleAuthRepository,
    private val googleDriveRepository: GoogleDriveRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val isManualTrigger = inputData.getBoolean(KEY_MANUAL_TRIGGER, false)
            Log.i(TAG, "doWork() started — isManualTrigger=$isManualTrigger")

            if (!isManualTrigger) {
                val enabled = settingsRepository
                    .getBoolean(SettingsKeys.AutoBackupEnabled)
                    .firstOrNull() ?: false
                Log.i(TAG, "AutoBackupEnabled=$enabled")
                if (!enabled) {
                    Log.i(TAG, "Auto backup disabled — skipping")
                    return Result.success()
                }
            }

            val backupTypeOrdinal = settingsRepository
                .getInt(SettingsKeys.AutoBackupType)
                .firstOrNull() ?: BackupType.SETTINGS_AND_DATABASE.ordinal

            val backupType = BackupType.entries.getOrElse(backupTypeOrdinal) {
                BackupType.SETTINGS_AND_DATABASE
            }

            val folderUri = settingsRepository
                .getString(SettingsKeys.AutoBackupFolderUri)
                .firstOrNull() ?: ""

            val deleteExisting = settingsRepository
                .getBoolean(SettingsKeys.AutoBackupDeleteExisting)
                .firstOrNull() ?: true

            val localEnabled = settingsRepository
                .getBoolean(SettingsKeys.AutoBackupLocalEnabled)
                .firstOrNull() ?: true

            val cloudEnabled = settingsRepository
                .getBoolean(SettingsKeys.AutoBackupCloudEnabled)
                .firstOrNull() ?: true

            Log.i(
                TAG,
                "localEnabled=$localEnabled cloudEnabled=$cloudEnabled folderUri=$folderUri backupType=$backupType deleteExisting=$deleteExisting"
            )

            if (localEnabled) {
                performLocalBackup(folderUri, deleteExisting, backupType)
            }

            if (cloudEnabled &&
                googleAuthRepository.isAvailable &&
                googleAuthRepository.googleUserState.value.isSignedIn
            ) {
                performCloudBackup(backupType)
            }

            Log.i(TAG, "doWork() finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork() uncaught exception", e)
            Result.success()
        }
    }

    private suspend fun performLocalBackup(
        folderUri: String,
        deleteExisting: Boolean,
        backupType: BackupType,
    ) {
        try {
            Log.i(TAG, "performLocalBackup() — folderUri=$folderUri")

            if (folderUri.isEmpty()) {
                Log.w(TAG, "performLocalBackup() — no folder URI set")
                settingsRepository.setString(
                    SettingsKeys.LastAutoBackupLocalError,
                    "No backup folder selected. Please select a folder in Backup Scheduler settings.",
                )
                return
            }

            val uri = folderUri.toUri()
            val dir = DocumentFile.fromTreeUri(applicationContext, uri)
            Log.i(TAG, "performLocalBackup() — dir=$dir canWrite=${dir?.canWrite()}")

            if (dir == null || !dir.canWrite()) {
                Log.w(TAG, "performLocalBackup() — cannot write to folder")
                settingsRepository.setString(
                    SettingsKeys.LastAutoBackupLocalError,
                    "Cannot write to backup folder. The folder may have been deleted or permissions revoked. Please re-select the folder.",
                )
                return
            }

            if (deleteExisting) {
                dir.listFiles().filter { file ->
                    val name = file.name ?: return@filter false
                    name.startsWith("backup_auto_") && name.endsWith(".ashellyou")
                }.forEach { it.delete() }
            }

            val bytes = backupAndRestoreRepository.generateCloudBackupBytes(backupType)
                ?: throw IllegalStateException("Failed to generate backup data. The app data may be corrupted.")

            val fileName = "backup_auto_${System.currentTimeMillis()}.ashellyou"
            val newFile = dir.createFile("application/octet-stream", fileName)
                ?: throw IllegalStateException("Failed to create file in backup folder. Storage may be full.")

            val fileUri = newFile.uri
            applicationContext.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.write(bytes)
            }
                ?: throw IllegalStateException("Failed to write to backup file. Storage may be full or permissions revoked.")

            val formattedTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

            settingsRepository.setString(
                SettingsKeys.LastAutoBackupLocalSuccessTime,
                formattedTime,
            )
            settingsRepository.setString(SettingsKeys.LastAutoBackupLocalError, "")
        } catch (e: Exception) {
            Log.e(TAG, "Local auto-backup failed", e)
            val reason = when {
                e is SecurityException -> "Permission denied. Please re-select the backup folder."
                e is java.io.IOException -> "Storage error: ${e.localizedMessage ?: "Failed to write backup file"}"
                e.localizedMessage != null -> e.localizedMessage!!
                else -> "Unknown error: ${e::class.simpleName}"
            }
            settingsRepository.setString(
                SettingsKeys.LastAutoBackupLocalError,
                reason,
            )
        }
    }

    private suspend fun performCloudBackup(backupType: BackupType) {
        try {
            // getHeadlessDriveService() fetches a token silently (no UI) and warms the
            // internal cache so uploadBackup() reuses the same Drive service.
            val authSucceeded = googleDriveRepository.getHeadlessDriveService() != null

            if (!authSucceeded) {
                settingsRepository.setString(
                    SettingsKeys.LastAutoBackupCloudError,
                    "Google Drive authorization failed. Please open the app and sign in again from Backup & Restore settings.",
                )
                return
            }

            val bytes = backupAndRestoreRepository.generateCloudBackupBytes(backupType)
                ?: throw IllegalStateException("Failed to generate backup data for cloud upload.")

            val uploaded = googleDriveRepository.uploadBackup(bytes, "backup_auto.ashellyou")
            if (!uploaded) {
                settingsRepository.setString(
                    SettingsKeys.LastAutoBackupCloudError,
                    "Upload to Google Drive failed. Check your internet connection and try again.",
                )
                return
            }

            val formattedTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

            settingsRepository.setString(
                SettingsKeys.LastAutoBackupCloudSuccessTime,
                formattedTime,
            )
            settingsRepository.setString(SettingsKeys.LastAutoBackupCloudError, "")
        } catch (e: Exception) {
            Log.e(TAG, "Cloud auto-backup failed", e)
            val reason = when {
                e is java.net.UnknownHostException -> "No internet connection. Cloud backup will retry on next schedule."
                e is java.io.IOException -> "Network error: ${e.localizedMessage ?: "Connection failed"}"
                e.localizedMessage != null -> e.localizedMessage!!
                else -> "Unknown error: ${e::class.simpleName}"
            }
            settingsRepository.setString(
                SettingsKeys.LastAutoBackupCloudError,
                reason,
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val channelId = "auto_backup_channel"
        val channel = NotificationChannel(
            channelId,
            "Auto Backup",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_adb)
            .setContentTitle("Backing up…")
            .setSilent(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        const val TAG = "AutoBackupWorker"
        const val KEY_MANUAL_TRIGGER = "manual_trigger"
        private const val NOTIFICATION_ID = 9001
    }
}
