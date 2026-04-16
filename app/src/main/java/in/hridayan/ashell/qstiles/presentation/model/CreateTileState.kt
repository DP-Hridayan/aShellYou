package `in`.hridayan.ashell.qstiles.presentation.model

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode

data class CreateTileState(
    val name: String = "",
    val command: String = "",
    val executionMode: Int = TileExecutionMode.SHIZUKU,
    val selectedIconId: String = "terminal",
    val suggestedIcons: List<String> = emptyList(),
    val iconSearchQuery : TextFieldValue = TextFieldValue(""),
    val tileId: Int = 0,
    val isUpdateMode: Boolean = false
)