package `in`.hridayan.ashell.qstiles.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tile_logs")
data class TileLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tileId: Int,
    val command: String,
    val output: String,
    val isSuccess: Boolean,
    val executionMode: Int,
    val timestamp: Long,
    val durationMs: Long = 0L,
    val errorType: Int = 0
)