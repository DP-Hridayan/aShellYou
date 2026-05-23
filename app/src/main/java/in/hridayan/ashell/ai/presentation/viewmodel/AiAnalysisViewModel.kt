package `in`.hridayan.ashell.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion
import `in`.hridayan.ashell.ai.domain.usecase.AnalyzeCommandUseCase
import `in`.hridayan.ashell.ai.presentation.model.AiAnalysisUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AI analysis bottom sheet.
 *
 * Manages the analysis lifecycle and exposes UI state for the bottom sheet.
 */
@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val analyzeCommandUseCase: AnalyzeCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val uiState: StateFlow<AiAnalysisUiState> = _uiState.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    /** Callback to apply a correction to the shell input field */
    var onApplyCorrection: ((String) -> Unit)? = null

    /**
     * Analyze a command and show the bottom sheet with results.
     *
     * @param command The shell/ADB command to analyze
     */
    fun analyzeCommand(command: String) {
        if (command.isBlank()) return

        _uiState.value = AiAnalysisUiState.Loading
        _showBottomSheet.value = true

        viewModelScope.launch {
            try {
                val result = analyzeCommandUseCase(command)
                _uiState.value = AiAnalysisUiState.Success(result)
            } catch (e: AnalyzeCommandUseCase.ModelNotInstalledException) {
                _uiState.value = AiAnalysisUiState.ModelNotInstalled
            } catch (e: Exception) {
                _uiState.value = AiAnalysisUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Apply a correction suggestion to the shell input field.
     */
    fun applyCorrection(correction: CorrectionSuggestion) {
        onApplyCorrection?.invoke(correction.suggestedCommand)
        dismiss()
    }

    /**
     * Dismiss the bottom sheet and reset state.
     */
    fun dismiss() {
        _showBottomSheet.value = false
        _uiState.value = AiAnalysisUiState.Idle
    }

    /**
     * Retry the last analysis after an error.
     */
    fun retry(command: String) {
        analyzeCommand(command)
    }
}
