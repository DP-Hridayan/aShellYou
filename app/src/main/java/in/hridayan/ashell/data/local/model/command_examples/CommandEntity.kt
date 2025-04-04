package `in`.hridayan.ashell.data.local.model.command_examples

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val description: String,
    val example: String = "",
    val isFavourite: Boolean = false,
    val useCount: Int = 0
)
