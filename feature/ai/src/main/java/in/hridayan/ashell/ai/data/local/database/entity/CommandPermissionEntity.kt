package `in`.hridayan.ashell.ai.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "command_permissions")
data class CommandPermissionEntity(
    @PrimaryKey
    val command: String,
    val isAlwaysAllowed: Boolean
)
