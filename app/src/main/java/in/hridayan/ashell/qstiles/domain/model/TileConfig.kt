package `in`.hridayan.ashell.qstiles.domain.model

data class TileConfig(
    val id: Int,
    val name: String,
    val command: String,
    val executionMode: Int,
    val iconId: String,
    val isActive: Boolean,
    val isCustom: Boolean
)