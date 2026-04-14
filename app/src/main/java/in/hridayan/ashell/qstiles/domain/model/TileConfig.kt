package `in`.hridayan.ashell.qstiles.domain.model

data class TileConfig(
    val tileId: Int, // 0–9 (fixed slots)
    val label: String,
    val command: String,
    val iconId: String,
    val executionMode: Int,
    val isEnabled: Boolean
)