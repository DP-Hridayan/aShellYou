package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val settings: Map<String, String?>? = null,
    val commands: List<CommandEntity>? = null,
    val backupTime: String
)
