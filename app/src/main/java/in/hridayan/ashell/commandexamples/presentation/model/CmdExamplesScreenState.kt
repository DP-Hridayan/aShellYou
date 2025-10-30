package `in`.hridayan.ashell.commandexamples.presentation.model

data class CmdExamplesScreenState(
    val commandField: CmdScreenInputFieldState.CommandInputFieldState = CmdScreenInputFieldState.CommandInputFieldState(),
    val descriptionField: CmdScreenInputFieldState.DescriptionInputFieldState = CmdScreenInputFieldState.DescriptionInputFieldState(),
    val labelField: CmdScreenInputFieldState.LabelInputFieldState = CmdScreenInputFieldState.LabelInputFieldState(),
    val search: CmdScreenUiState.Search = CmdScreenUiState.Search(),
)