package `in`.hridayan.ashell.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val command: String
)
