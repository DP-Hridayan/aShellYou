package `in`.hridayan.ashell.settings.data.repository


import `in`.hridayan.ashell.core.common.SettingsKeys

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.settings.data.utils.EncryptionHelper
import `in`.hridayan.ashell.core.common.domain.model.BackupData
import `in`.hridayan.ashell.core.common.domain.model.BackupMode
import `in`.hridayan.ashell.core.common.domain.model.BackupType
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BackupAndRestoreRepositoryImpl @Inject constructor(
    private val json: Json,
    private val backupProviders: Set<@JvmSuppressWildcards BackupProvider>,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationContext private val context: Context
) : BackupAndRestoreRepository {

    override suspend fun backupToDevice(uri: Uri, type: BackupType): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val backupData = getBackupData(
                    type = type,
                    backupMode = BackupMode.LOCAL_DEVICE
                )
                val jsonData = json.encodeToString(BackupData.serializer(), backupData)
                val encryptedBytes = EncryptionHelper.encrypt(jsonData.toByteArray())

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(encryptedBytes)
                }

                settingsRepository.setString(
                    SettingsKeys.LastLocalBackupTime,
                    backupData.backupTime
                )

                settingsRepository.setString(
                    SettingsKeys.LastLocalBackupType,
                    backupData.backupType
                )

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override suspend fun restoreDataFromFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream =
                context.contentResolver.openInputStream(uri) ?: return@withContext false
            val encryptedBytes = inputStream.readBytes()
            val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
            val jsonString = decryptedBytes.toString(Charsets.UTF_8)
            val restoredData = json.decodeFromString(BackupData.serializer(), jsonString)

            saveRestoredData(restoredData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun getBackupData(
        type: BackupType,
        backupMode: BackupMode
    ): BackupData {
        val payloads = mutableMapOf<String, JsonElement>()

        for (provider in backupProviders) {
            val shouldBackup = when (type) {
                BackupType.SETTINGS_ONLY -> provider.featureId == "settings"
                BackupType.DATABASE_ONLY -> provider.featureId != "settings"
                BackupType.SETTINGS_AND_DATABASE -> true
            }

            if (shouldBackup) {
                provider.getBackupData()?.let { data ->
                    payloads[provider.featureId] = data
                }
            }
        }

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val backupTime = LocalDateTime.now().format(formatter)

        return BackupData(
            payloads = payloads,
            backupTime = backupTime,
            backupType = type.name,
            backupMode = backupMode.name
        )
    }

    override suspend fun getBackupTimeFromFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream =
                context.contentResolver.openInputStream(uri) ?: return@withContext null
            val encryptedBytes = inputStream.readBytes()
            val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
            val jsonString = decryptedBytes.toString(Charsets.UTF_8)
            val data = json.decodeFromString(BackupData.serializer(), jsonString)
            data.backupTime
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getBackupTypeFromFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream =
                context.contentResolver.openInputStream(uri) ?: return@withContext null
            val encryptedBytes = inputStream.readBytes()
            val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
            val jsonString = decryptedBytes.toString(Charsets.UTF_8)
            val data = json.decodeFromString(BackupData.serializer(), jsonString)
            data.backupType
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun saveRestoredData(data: BackupData) {
        val legacyDataMap = mapOf(
            "settings" to data.settings,
            "commands" to data.commands,
            "bookmarks" to data.bookmarks,
            "tiles" to data.tiles,
            "tileLogs" to data.tileLogs
        )

        for (provider in backupProviders) {
            val providerData = data.payloads?.get(provider.featureId)
            
            // Only restore if we have data for this provider, either new payload or legacy
            if (providerData != null || legacyDataMap[provider.featureId] != null) {
                provider.restoreData(providerData) { legacyKey ->
                    legacyDataMap[legacyKey]
                }
            }
        }
    }

    override suspend fun generateCloudBackupBytes(type: BackupType): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val backupData = getBackupData(
                    type = type,
                    backupMode = BackupMode.GOOGLE_DRIVE
                )
                val jsonData = json.encodeToString(BackupData.serializer(), backupData)
                EncryptionHelper.encrypt(jsonData.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun restoreFromBytes(encryptedBytes: ByteArray): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
                val jsonString = decryptedBytes.toString(Charsets.UTF_8)
                val restoredData = json.decodeFromString(BackupData.serializer(), jsonString)
                saveRestoredData(restoredData)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    override suspend fun getBackupTimeFromBytes(encryptedBytes: ByteArray): String? =
        withContext(Dispatchers.IO) {
            try {
                val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
                val jsonString = decryptedBytes.toString(Charsets.UTF_8)
                val restoredData = json.decodeFromString(BackupData.serializer(), jsonString)
                restoredData.backupTime
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    override suspend fun getBackupTypeFromBytes(encryptedBytes: ByteArray): String? =
        withContext(Dispatchers.IO) {
            try {
                val decryptedBytes = EncryptionHelper.decrypt(encryptedBytes)
                val jsonString = decryptedBytes.toString(Charsets.UTF_8)
                val restoredData = json.decodeFromString(BackupData.serializer(), jsonString)
                restoredData.backupType
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
