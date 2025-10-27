package `in`.hridayan.ashell.commandexamples.presentation.model

sealed class InputFieldState() {
    /**
     * This input field is in the command examples screen AddCommandDialog and EditCommandDialog command input field
     */
    data class CommandInputFieldState(
        val fieldText: String = "",
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : InputFieldState()

    /**
     * This input field is in the command examples screen AddCommandDialog and EditCommandDialog description input field
     */
    data class DescriptionInputFieldState(
        val fieldText: String = "",
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : InputFieldState()

    /**
     * This input field is in the command examples screen AddCommandDialog and EditCommandDialog label input field
     */
    data class LabelInputFieldState(
        val fieldText: String = "",
        val labels: List<String> = emptyList(),
        val isError: Boolean = false,
        val errorMessage: String = ""
    ) : InputFieldState()
}