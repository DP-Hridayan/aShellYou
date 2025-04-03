package `in`.hridayan.ashell.data.local.model.command_examples

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commandLabels")
data class LabelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String
)