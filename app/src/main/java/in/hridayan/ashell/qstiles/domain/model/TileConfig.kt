package `in`.hridayan.ashell.qstiles.domain.model

data class TileConfig(
    val id: Int,
    val name: String,
    val iconId: String,
    val command: String,
    val executionMode: Int,
    val isActive: Boolean,
    val isCustom: Boolean,
    val slotIndex: Int? = null,
    val timeoutMs: Long? = null
)