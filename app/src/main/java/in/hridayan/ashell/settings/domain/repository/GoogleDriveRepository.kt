package `in`.hridayan.ashell.settings.domain.repository

interface GoogleDriveRepository {
    /** Upload encrypted backup bytes to appDataFolder, overwriting any existing backup. */
    suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean

    /** Download the latest backup from appDataFolder. Returns bytes + backup file name, or null if none exists. */
    suspend fun downloadBackup(): Pair<ByteArray, String>?

    /** Delete all backups from appDataFolder. */
    suspend fun deleteAllBackups(): Boolean
}
