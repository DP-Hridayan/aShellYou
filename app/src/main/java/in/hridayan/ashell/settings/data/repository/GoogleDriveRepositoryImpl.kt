package `in`.hridayan.ashell.settings.data.repository

import android.accounts.Account
import android.content.Context
import android.content.IntentSender
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

    private val _consentRequired = MutableSharedFlow<IntentSender>()
    override val consentRequired: SharedFlow<IntentSender> = _consentRequired.asSharedFlow()

    // Cached Drive service to reuse across operations (avoids eventual consistency issues)
    @Volatile
    private var cachedDriveService: Drive? = null
    @Volatile
    private var cachedEmail: String? = null

    // After user grants consent, the next authorize() call should succeed silently
    @Volatile
    private var consentJustGranted = false

    // Flag to distinguish 'consent needed → null' from 'genuine failure → null'
    @Volatile
    override var isConsentPending = false
        private set

    override fun onConsentGranted() {
        Log.d(TAG, "onConsentGranted: consent was just granted, next authorize should succeed")
        consentJustGranted = true
        isConsentPending = false
        // Invalidate cache so next call gets a fresh authorized service
        cachedDriveService = null
    }

    override suspend fun ensureAuthorized(): Boolean {
        Log.d(TAG, "ensureAuthorized: checking Drive scope...")
        return buildDriveService() != null
    }

    /**
     * Authorize the drive.appdata scope and build a Drive service.
     * Caches the service to reuse the same access token across operations.
     * If consent is needed, emits via consentRequired and returns null.
     */
    private suspend fun buildDriveService(): Drive? {
        val email = googleAuthRepository.getAccountEmail()
        if (email == null) {
            Log.e(TAG, "buildDriveService: no email — not signed in")
            return null
        }

        // Return cached service if email hasn't changed
        cachedDriveService?.let { cached ->
            if (cachedEmail == email) {
                Log.d(TAG, "buildDriveService: reusing cached Drive service for $email")
                return cached
            }
        }

        val authResult: AuthorizationResult
        try {
            Log.d(TAG, "buildDriveService: requesting drive.appdata scope authorization for $email...")
            val authRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .setAccount(Account(email, "com.google"))
                .build()

            authResult = suspendCancellableCoroutine { continuation ->
                Identity.getAuthorizationClient(context)
                    .authorize(authRequest)
                    .addOnSuccessListener { result ->
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            if (authResult.hasResolution()) {
                val pendingIntent = authResult.pendingIntent
                if (pendingIntent != null) {
                    Log.d(TAG, "buildDriveService: scope needs user consent — emitting consent intent")
                    isConsentPending = true
                    _consentRequired.emit(pendingIntent.intentSender)
                } else {
                    Log.e(TAG, "buildDriveService: hasResolution but no pendingIntent")
                }
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "buildDriveService: authorization failed", e)
            return null
        }

        val accessToken = authResult.accessToken
        if (accessToken == null) {
            Log.e(TAG, "buildDriveService: accessToken is null after authorization")
            return null
        }

        Log.d(TAG, "buildDriveService: got accessToken, building Drive service for $email")

        val requestInitializer = HttpRequestInitializer { request: HttpRequest ->
            request.headers.authorization = "Bearer $accessToken"
        }

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        )
            .setApplicationName(APP_NAME)
            .build()

        // Cache the service
        cachedDriveService = driveService
        cachedEmail = email

        return driveService
    }

    override suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val driveService = buildDriveService() ?: return@withContext false

                Log.d(TAG, "uploadBackup: deleting existing backups...")
                deleteAllBackupsInternal(driveService)

                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = fileName
                    parents = listOf(APP_DATA_FOLDER)
                }

                val mediaContent = ByteArrayContent(MIME_TYPE, data)

                val created = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute()

                Log.d(TAG, "uploadBackup: SUCCESS — fileId=${created.id}, name=${created.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "uploadBackup: FAILED", e)
                false
            }
        }

    override suspend fun downloadBackup(): Pair<ByteArray, String>? =
        withContext(Dispatchers.IO) {
            try {
                val driveService = buildDriveService() ?: return@withContext null

                Log.d(TAG, "downloadBackup: listing files in appDataFolder...")
                val result = driveService.files()
                    .list()
                    .setSpaces(APP_DATA_FOLDER)
                    .setFields("files(id, name, createdTime)")
                    .setOrderBy("createdTime desc")
                    .setPageSize(1)
                    .execute()

                val files = result.files
                Log.d(TAG, "downloadBackup: found ${files?.size ?: 0} files")

                val file = files?.firstOrNull()
                if (file == null) {
                    Log.d(TAG, "downloadBackup: no backup file found")
                    return@withContext null
                }

                Log.d(TAG, "downloadBackup: downloading file '${file.name}' (id=${file.id})")

                val outputStream = ByteArrayOutputStream()
                driveService.files()
                    .get(file.id)
                    .executeMediaAndDownloadTo(outputStream)

                val bytes = outputStream.toByteArray()
                Log.d(TAG, "downloadBackup: downloaded ${bytes.size} bytes")

                Pair(bytes, file.name)
            } catch (e: Exception) {
                Log.e(TAG, "downloadBackup: FAILED", e)
                null
            }
        }

    override suspend fun deleteAllBackups(): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService() ?: return@withContext false
            deleteAllBackupsInternal(driveService)
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteAllBackups: FAILED", e)
            false
        }
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
