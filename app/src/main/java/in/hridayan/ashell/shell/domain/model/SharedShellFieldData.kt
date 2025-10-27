package `in`.hridayan.ashell.shell.domain.model

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

object SharedShellFieldData {
    var commandText = MutableStateFlow(TextFieldValue(""))
    var commandError = MutableStateFlow(false)
    var commandOutput = MutableStateFlow<List<CommandResult>>(emptyList())
    var shellState = MutableStateFlow<ShellState>(ShellState.Free)
    var history = MutableStateFlow<List<String>>(emptyList())
    var isSearchBarVisible = MutableStateFlow(false)
    var searchQuery = MutableStateFlow("")
    var buttonGroupHeight = MutableStateFlow(0.dp)
}
