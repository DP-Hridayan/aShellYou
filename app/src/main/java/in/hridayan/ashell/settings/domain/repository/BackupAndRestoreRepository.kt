package `in`.hridayan.ashell.settings.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.settings.domain.model.BackupType

interface BackupAndRestoreRepository {
    suspend fun backupToDevice(uri: Uri, type: BackupType): Boolean
    suspend fun restoreDataFromFile(uri: Uri): Boolean
    suspend fun getBackupTimeFromFile(uri: Uri): String?
    suspend fun getBackupTypeFromFile(uri: Uri) : String?

    /** Generate encrypted backup bytes without needing a Uri (for cloud backup) */
    suspend fun generateCloudBackupBytes(type: BackupType): ByteArray?

    /** Restore from raw encrypted bytes without needing a Uri (for cloud restore) */
    suspend fun restoreFromBytes(encryptedBytes: ByteArray): Boolean

    /** Extract backup time from encrypted bytes (for cloud restore confirm dialog) */
    suspend fun getBackupTimeFromBytes(encryptedBytes: ByteArray): String?
    suspend fun getBackupTypeFromBytes(encryptedBytes: ByteArray) : String?
}
