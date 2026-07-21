package `in`.hridayan.ashell.qstiles.domain.model

data class TileLog(
    val id: Long,
    val tileId: Int,
    val command: String,
    val output: String,
    val isSuccess: Boolean,
    val executionMode: Int,
    val timestamp: Long,
    val durationMs: Long = 0L,
    val errorType: Int = 0
)
