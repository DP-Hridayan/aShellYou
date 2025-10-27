package `in`.hridayan.ashell.commandexamples.presentation.model

import `in`.hridayan.ashell.commandexamples.presentation.model.InputFieldState
import `in`.hridayan.ashell.commandexamples.presentation.model.UiState

data class CommandExamplesScreenState(
    val commandField: InputFieldState.CommandInputFieldState = InputFieldState.CommandInputFieldState(),
    val descriptionField: InputFieldState.DescriptionInputFieldState = InputFieldState.DescriptionInputFieldState(),
    val labelField: InputFieldState.LabelInputFieldState = InputFieldState.LabelInputFieldState(),
    val search: UiState.Search = UiState.Search(),
)