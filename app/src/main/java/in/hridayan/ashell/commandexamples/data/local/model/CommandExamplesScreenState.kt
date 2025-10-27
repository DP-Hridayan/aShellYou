package `in`.hridayan.ashell.commandexamples.data.local.model

data class CommandExamplesScreenState(
    val commandField: InputFieldState.CommandInputFieldState = InputFieldState.CommandInputFieldState(),
    val descriptionField: InputFieldState.DescriptionInputFieldState = InputFieldState.DescriptionInputFieldState(),
    val labelField: InputFieldState.LabelInputFieldState = InputFieldState.LabelInputFieldState(),
    val search: UiState.Search = UiState.Search(),
)