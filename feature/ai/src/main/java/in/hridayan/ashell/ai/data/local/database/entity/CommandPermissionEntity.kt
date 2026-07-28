package `in`.hridayan.ashell.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_permissions")
data class CommandPermissionEntity(
    @PrimaryKey
    val command: String, // Exact command, e.g., "pm uninstall"
    val isAlwaysAllowed: Boolean
)
