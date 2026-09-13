package `in`.hridayan.ashell.qstiles.data.model

data class TileIcon(
    val id: String,
    val resId: Int = 0,
    val keywords: List<String>,
    val codepoint: Int? = null,
    val isBundled: Boolean = true
)
