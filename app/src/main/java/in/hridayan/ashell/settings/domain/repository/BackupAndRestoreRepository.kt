package `in`.hridayan.ashell.settings.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.settings.domain.model.BackupOption

interface BackupAndRestoreRepository {
    suspend fun backupToDevice(uri: Uri, option: BackupOption): Boolean
    suspend fun restoreDataFromFile(uri: Uri): Boolean
    suspend fun getBackupTimeFromFile(uri: Uri): String?

    /** Generate encrypted backup bytes without needing a Uri (for cloud backup) */
    suspend fun generateBackupBytes(option: BackupOption): ByteArray?

    /** Restore from raw encrypted bytes without needing a Uri (for cloud restore) */
    suspend fun restoreFromBytes(encryptedBytes: ByteArray): Boolean

    /** Extract backup time from encrypted bytes (for cloud restore confirm dialog) */
    suspend fun getBackupTimeFromBytes(encryptedBytes: ByteArray): String?
}
