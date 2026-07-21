package `in`.hridayan.ashell.core.common.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BackupData(
    val payloads: Map<String, JsonElement>? = null,
    
    // Legacy fields for backward compatibility when restoring old backups
    val settings: JsonElement? = null,
    val commands: JsonElement? = null,
    val bookmarks: JsonElement? = null,
    val tiles: JsonElement? = null,
    val tileLogs: JsonElement? = null,
    
    val backupTime: String,
    val backupType: String,
    val backupMode: String
)
