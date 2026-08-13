package `in`.hridayan.ashell.settings.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "custom_fonts")
data class CustomFontEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val displayName: String,
    val filePath: String,
    val importedAt: Long = System.currentTimeMillis()
)
