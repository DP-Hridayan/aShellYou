package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.DriveAuthEvent
import kotlinx.coroutines.flow.SharedFlow

interface GoogleDriveRepository {
    /** Whether Google Drive backup is available in this build flavor. */
    val isAvailable: Boolean

    /** Emits an IntentSender when user consent is needed for the Drive scope. */
    val authEvents: SharedFlow<DriveAuthEvent>

    /** True when a consent dialog was just triggered and we're waiting for the user to respond. */
    val isConsentPending: Boolean

    /** Upload encrypted backup bytes to appDataFolder, overwriting any existing backup. */
    suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean

    /** Download the latest backup from appDataFolder. Returns bytes + backup file name, or null if none exists. */
    suspend fun downloadBackup(): Pair<ByteArray, String>?

    /** Delete all backups from appDataFolder. */
    suspend fun deleteAllBackups(): Boolean

    /** Pre-authorize the Drive scope. If consent is needed, emits via consentRequired. Returns true if already authorized. */
    suspend fun ensureAuthorized(): Boolean

    /** Called after the user grants consent. Caches the authorized result for retry. */
    fun onConsentGranted()
}
