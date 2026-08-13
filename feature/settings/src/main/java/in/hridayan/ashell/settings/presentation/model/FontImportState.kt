package `in`.hridayan.ashell.settings.presentation.model

sealed class FontImportState {
    data object Idle : FontImportState()
    data object Validating : FontImportState()
    data object InvalidFile : FontImportState()
    data class NamingPrompt(
        val prefilledName: String,
        val tempFilePath: String
    ) : FontImportState()

    data object Saving : FontImportState()
}
