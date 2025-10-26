package `in`.hridayan.ashell.commandexamples.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * @param command The ADB command
 * @param description Description of what the ADB command does
 * @param isFavourite Boolean flag to indicate whether the command is set as favourite
 * @param useCount Shows how many time the command has been used
 * @param labels List of label that indicates the category or nature of the ADB command
 */
@Serializable
@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val description: String,
    val isFavourite: Boolean = false,
    val useCount: Int = 0,
    val labels: List<String> = emptyList()
)