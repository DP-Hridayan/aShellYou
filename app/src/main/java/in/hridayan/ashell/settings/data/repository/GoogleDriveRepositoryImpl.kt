package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class GoogleDriveRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val googleAuthRepository: GoogleAuthRepository
) : GoogleDriveRepository {

    companion object {
        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val MIME_TYPE = "application/octet-stream"
        private const val APP_NAME = "aShellYou"
    }

    private fun buildDriveService(): Drive? {
        val email = googleAuthRepository.getAccountEmail() ?: return null

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccountName = email
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(APP_NAME)
            .build()
    }

    override suspend fun uploadBackup(data: ByteArray, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val driveService = buildDriveService() ?: return@withContext false

                // Delete existing backups first (single overwrite strategy)
                deleteAllBackupsInternal(driveService)

                // Create new backup file in appDataFolder
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = fileName
                    parents = listOf(APP_DATA_FOLDER)
                }

                val mediaContent = ByteArrayContent(MIME_TYPE, data)

                driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute()

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override suspend fun downloadBackup(): Pair<ByteArray, String>? =
        withContext(Dispatchers.IO) {
            try {
                val driveService = buildDriveService() ?: return@withContext null

                // Find the latest .ashellyou backup file
                val result = driveService.files()
                    .list()
                    .setSpaces(APP_DATA_FOLDER)
                    .setFields("files(id, name, createdTime)")
                    .setOrderBy("createdTime desc")
                    .setPageSize(1)
                    .execute()

                val file = result.files?.firstOrNull() ?: return@withContext null

                // Download the file content
                val outputStream = ByteArrayOutputStream()
                driveService.files()
                    .get(file.id)
                    .executeMediaAndDownloadTo(outputStream)

                Pair(outputStream.toByteArray(), file.name)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun deleteAllBackups(): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService() ?: return@withContext false
            deleteAllBackupsInternal(driveService)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun deleteAllBackupsInternal(driveService: Drive) {
        val result = driveService.files()
            .list()
            .setSpaces(APP_DATA_FOLDER)
            .setFields("files(id)")
            .execute()

        result.files?.forEach { file ->
            driveService.files().delete(file.id).execute()
        }
    }
}
