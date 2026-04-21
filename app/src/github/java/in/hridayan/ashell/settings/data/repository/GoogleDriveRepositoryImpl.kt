package `in`.hridayan.ashell.settings.data.repository

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.domain.model.DriveAuthEvent
import `in`.hridayan.ashell.settings.domain.model.DriveOperationResult
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleDriveRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val googleAuthRepository: GoogleAuthRepository
) : GoogleDriveRepository {

    companion object {
        private const val TAG = "GoogleDrive"
        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val MIME_TYPE = "application/octet-stream"
        private const val APP_NAME = "aShellYou"
    }

    override val isAvailable: Boolean = true

    private val _authEvents = MutableSharedFlow<DriveAuthEvent>()
    override val authEvents: SharedFlow<DriveAuthEvent> = _authEvents.asSharedFlow()

    @Volatile
    private var cachedDriveService: Drive? = null

    @Volatile
    private var cachedEmail: String? = null

    @Volatile
    override var isConsentPending = false
        private set

    override fun onConsentGranted() {
        Log.d(TAG, "onConsentGranted: invalidating cache")
        isConsentPending = false
        cachedDriveService = null
    }

    override suspend fun ensureAuthorized(): Boolean {
        return when (getDriveService()) {
            is DriveOperationResult.Success -> true
            else -> false
        }
    }

    override suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            when (val result = getDriveService()) {

                is DriveOperationResult.Success -> {
                    try {
                        val drive = result.data

                        deleteAllBackupsInternal(drive)

                        val fileMetadata = com.google.api.services.drive.model.File().apply {
                            name = fileName
                            parents = listOf(APP_DATA_FOLDER)
                        }

                        val mediaContent = ByteArrayContent(MIME_TYPE, data)

                        val created = drive.files()
                            .create(fileMetadata, mediaContent)
                            .setFields("id, name")
                            .execute()

                        Log.d(TAG, "uploadBackup: SUCCESS id=${created.id}")
                        true

                    } catch (e: Exception) {
                        Log.e(TAG, "uploadBackup: FAILED", e)
                        false
                    }
                }

                else -> false
            }
        }

    override suspend fun downloadBackup(): Pair<ByteArray, String>? =
        withContext(Dispatchers.IO) {
            when (val result = getDriveService()) {

                is DriveOperationResult.Success -> {
                    try {
                        val drive = result.data

                        val files = drive.files()
                            .list()
                            .setSpaces(APP_DATA_FOLDER)
                            .setFields("files(id, name, createdTime)")
                            .setOrderBy("createdTime desc")
                            .setPageSize(1)
                            .execute()
                            .files

                        val file = files?.firstOrNull() ?: return@withContext null

                        val output = ByteArrayOutputStream()
                        drive.files()
                            .get(file.id)
                            .executeMediaAndDownloadTo(output)

                        Pair(output.toByteArray(), file.name)

                    } catch (e: Exception) {
                        Log.e(TAG, "downloadBackup: FAILED", e)
                        null
                    }
                }

                else -> null
            }
        }

    override suspend fun deleteAllBackups(): Boolean =
        withContext(Dispatchers.IO) {
            when (val result = getDriveService()) {

                is DriveOperationResult.Success -> {
                    try {
                        deleteAllBackupsInternal(result.data)
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "deleteAllBackups: FAILED", e)
                        false
                    }
                }

                else -> false
            }
        }

    private suspend fun getDriveService(): DriveOperationResult<Drive> {
        val email = googleAuthRepository.getAccountEmail()
            ?: return DriveOperationResult.Error(Exception("Not signed in"))

        // reuse cache
        cachedDriveService?.let {
            if (cachedEmail == email) {
                return DriveOperationResult.Success(it)
            }
        }

        return when (val auth = authorize(email)) {

            is DriveOperationResult.Success -> {
                val service = createDriveService(auth.data)
                cachedDriveService = service
                cachedEmail = email
                DriveOperationResult.Success(service)
            }

            is DriveOperationResult.ConsentRequired -> auth
            is DriveOperationResult.Error -> auth
        }
    }

    private suspend fun authorize(email: String): DriveOperationResult<String> {
        return try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .setAccount(Account(email, "com.google"))
                .build()

            val result = suspendCancellableCoroutine<AuthorizationResult> { cont ->
                Identity.getAuthorizationClient(context)
                    .authorize(request)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }

            if (result.hasResolution()) {
                val intent = result.pendingIntent?.intentSender
                if (intent != null) {
                    Log.d(TAG, "authorize: consent required")
                    isConsentPending = true
                    _authEvents.emit(DriveAuthEvent.ConsentRequired(intent))
                    return DriveOperationResult.ConsentRequired
                }
                return DriveOperationResult.Error(Exception("Missing pendingIntent"))
            }

            val token = result.accessToken
                ?: return DriveOperationResult.Error(Exception("AccessToken null"))

            DriveOperationResult.Success(token)

        } catch (e: Exception) {
            Log.e(TAG, "authorize: FAILED", e)
            DriveOperationResult.Error(e)
        }
    }

    private fun createDriveService(token: String): Drive {
        val initializer = HttpRequestInitializer { request: HttpRequest ->
            request.headers.authorization = "Bearer $token"
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            initializer
        )
            .setApplicationName(APP_NAME)
            .build()
    }

    private fun deleteAllBackupsInternal(driveService: Drive) {
        val result = driveService.files()
            .list()
            .setSpaces(APP_DATA_FOLDER)
            .setFields("files(id)")
            .execute()

        Log.d(TAG, "deleteAllBackups: found ${result.files?.size ?: 0} files to delete")

        result.files?.forEach { file ->
            driveService.files().delete(file.id).execute()
        }
    }
}
