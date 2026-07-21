package `in`.hridayan.ashell.ai.presentation.model

import `in`.hridayan.ashell.ai.domain.model.AnalysisResult

/**
 * UI state for the AI analysis bottom sheet.
 */
sealed interface AiAnalysisUiState {
    /** No analysis in progress */
    data object Idle : AiAnalysisUiState

    /** Analysis is running — show skeleton loading */
    data object Loading : AiAnalysisUiState

    /** Analysis completed successfully */
    data class Success(val result: AnalysisResult) : AiAnalysisUiState

    /** Analysis failed with an error */
    data class Error(val message: String) : AiAnalysisUiState

    /** No AI model is installed — prompt download */
    data object ModelNotInstalled : AiAnalysisUiState
}
