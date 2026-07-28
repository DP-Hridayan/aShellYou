package `in`.hridayan.ashell.core.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult

/**
 * UI state for the AI analysis bottom sheet.
 */
@Immutable
sealed interface AiAnalysisUiState {
    /** No analysis in progress */
    data object Idle : AiAnalysisUiState

    /** Analysis is running — show skeleton loading */
    data object Loading : AiAnalysisUiState

    /** Analysis completed successfully */
    data class Success(val result: AnalysisResult) : AiAnalysisUiState

    /** Analysis failed with an error */
    data class Error(val message: String) : AiAnalysisUiState
}
