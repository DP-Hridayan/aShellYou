package `in`.hridayan.ashell.settings.data.repository

import `in`.hridayan.ashell.settings.domain.model.DriveAuthEvent
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/** No-op implementation used in the fdroid flavor — Google Drive is not available. */
class FdroidGoogleDriveRepositoryImpl @Inject constructor() : GoogleDriveRepository {

    override val isAvailable: Boolean = false

    private val _authEvents = MutableSharedFlow<DriveAuthEvent>()
    override val authEvents: SharedFlow<DriveAuthEvent> = _authEvents.asSharedFlow()

    override val isConsentPending: Boolean = false

    override fun onConsentGranted() {
        // no-op
    }

    override suspend fun ensureAuthorized(): Boolean = false

    override suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean = false

    override suspend fun downloadBackup(): Pair<ByteArray, String>? = null

    override suspend fun deleteAllBackups(): Boolean = false
}
