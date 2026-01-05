package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.utils.EncryptionHelper
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.BackupData
import `in`.hridayan.ashell.settings.domain.model.BackupOption
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BackupAndRestoreRepositoryImpl @Inject constructor(
    private val json: Json,
    private val commandRepository: CommandRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationContext private val context: Context
) : BackupAndRestoreRepository {

    override suspend fun backupDataToFile(uri: Uri, option: BackupOption): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val backupData = getBackupData(option)
                val jsonData = json.encodeToString(BackupData.serializer(), backupData)
                val encryptedBytes = EncryptionHelper.encrypt(jsonData.toByteArray())

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(encryptedBytes)
                }

                settingsRepository.setString(SettingsKeys.LAST_BACKUP_TIME, backupData.backupTime)

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

    private suspend fun getBackupData(option: BackupOption): BackupData {
        val settings =
            if (option == BackupOption.SETTINGS_ONLY || option == BackupOption.SETTINGS_AND_DATABASE)
                getSettingsMap() else null

        val commands =
            if (option == BackupOption.DATABASE_ONLY || option == BackupOption.SETTINGS_AND_DATABASE)
                commandRepository.getAllCommandsOnce() else null

        val bookmarks =
            if (option == BackupOption.DATABASE_ONLY || option == BackupOption.SETTINGS_AND_DATABASE)
                bookmarkRepository.getBookmarksSorted(SortType.AZ) else null

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        val backupTime = LocalDateTime.now().format(formatter)

        return BackupData(
            settings = settings,
            commands = commands,
            bookmarks = bookmarks,
            backupTime = backupTime
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

    private suspend fun getSettingsMap(): Map<String, String?> {
        val prefs = settingsRepository.getCurrentSettings()
        return prefs.mapValues { it.value?.toString() }
    }

    private suspend fun saveRestoredData(data: BackupData) {
        data.commands?.let {
            commandRepository.deleteAllCommands()
            commandRepository.insertAllCommands(it)
        }
        data.bookmarks?.let {
            bookmarkRepository.deleteAllBookmarks()
            bookmarkRepository.insertAllBookmarks(it)
        }
        data.settings?.let { restoreSettings(it) }
    }

    private suspend fun restoreSettings(settings: Map<String, String?>) {
        settingsRepository.resetAndRestoreDefaults()

        settings.forEach { (key, value) ->
            if (key == SettingsKeys.SAVED_VERSION_CODE.name) return@forEach

            val settingKey = SettingsKeys.entries.find { it.name == key } ?: return@forEach

            value?.let {
                when (settingKey.default) {
                    is Boolean -> settingsRepository.setBoolean(
                        settingKey,
                        it.toBooleanStrictOrNull() ?: return@forEach
                    )

                    is Int -> settingsRepository.setInt(
                        settingKey,
                        it.toIntOrNull() ?: return@forEach
                    )

                    is Float -> settingsRepository.setFloat(
                        settingKey,
                        it.toFloatOrNull() ?: return@forEach
                    )
                }
            }
        }
    }
}