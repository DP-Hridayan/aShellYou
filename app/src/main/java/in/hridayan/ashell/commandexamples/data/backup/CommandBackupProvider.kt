package `in`.hridayan.ashell.commandexamples.data.backup

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class CommandBackupProvider @Inject constructor(
    private val commandRepository: CommandRepository,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "commands"

    override suspend fun getBackupData(): JsonElement? {
        val commands = commandRepository.getAllCommandsOnce()
        if (commands.isEmpty()) return null
        return json.encodeToJsonElement(commands)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val jsonData = data ?: legacyData("commands") ?: return
        
        try {
            val commands = json.decodeFromJsonElement<List<CommandEntity>>(jsonData)
            if (commands.isNotEmpty()) {
                commandRepository.deleteAllCommands()
                commandRepository.insertAllCommands(commands)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
