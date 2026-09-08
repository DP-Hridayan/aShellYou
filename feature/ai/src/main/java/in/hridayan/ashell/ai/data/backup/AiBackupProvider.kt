package `in`.hridayan.ashell.ai.data.backup

import `in`.hridayan.ashell.ai.data.local.database.dao.ChatDao
import `in`.hridayan.ashell.ai.data.local.database.dao.CommandPermissionDao
import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class AiBackupProvider @Inject constructor(
    private val chatDao: ChatDao,
    private val commandPermissionDao: CommandPermissionDao,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "ai_chats"

    override suspend fun getBackupData(): JsonElement? {
        val sessions = chatDao.getAllSessionsSync()
        val messages = chatDao.getAllMessagesSync()
        val permissions = commandPermissionDao.getAllPermissionsSync()

        if (sessions.isEmpty() && permissions.isEmpty()) return null

        val backupData = AiBackupData(
            sessions = sessions,
            messages = messages,
            commandPermissions = permissions
        )

        return json.encodeToJsonElement(backupData)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val jsonData = data ?: legacyData("ai_chats") ?: return

        try {
            val backupData = json.decodeFromJsonElement<AiBackupData>(jsonData)

            // Restore sessions and messages
            if (backupData.sessions.isNotEmpty()) {
                chatDao.deleteAllSessions()
                chatDao.insertSessions(backupData.sessions)
                chatDao.insertMessages(backupData.messages)
            }

            // Restore command permissions
            if (backupData.commandPermissions.isNotEmpty()) {
                commandPermissionDao.deleteAllPermissions()
                commandPermissionDao.insertPermissions(backupData.commandPermissions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
