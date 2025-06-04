package `in`.hridayan.ashell.settings.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.settings.domain.model.BackupOption

interface BackupAndRestoreRepository {
    suspend fun backupDataToFile(uri: Uri, option: BackupOption): Boolean
    suspend fun restoreDataFromFile(uri: Uri): Boolean
    suspend fun getBackupTimeFromFile(uri: Uri): String?
}
