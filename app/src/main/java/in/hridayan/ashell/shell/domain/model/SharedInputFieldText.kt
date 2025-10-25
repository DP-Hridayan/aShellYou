package `in`.hridayan.ashell.shell.domain.model

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.MutableStateFlow

object SharedInputFieldText {
    var commandText = MutableStateFlow(TextFieldValue(""))
}
