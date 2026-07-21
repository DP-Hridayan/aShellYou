package `in`.hridayan.ashell.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion
import `in`.hridayan.ashell.ai.domain.usecase.AnalyzeCommandUseCase
import `in`.hridayan.ashell.ai.presentation.model.AiAnalysisUiState
import kotlinx.coroutines.Job
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

    /** The currently running analysis job, if any. */
    private var analysisJob: Job? = null

    /**
     * Analyze a command and show the bottom sheet with results.
     *
     * Cancels any previously running analysis before starting a new one
     * to prevent mutex contention ANRs in the inference engine.
     *
     * @param command The shell/ADB command to analyze
     */
    fun analyzeCommand(command: String) {
        if (command.isBlank()) return

        // Cancel any in-flight analysis to release the inference engine mutex.
        // The native JNI call isn't interruptible, but cancelling the coroutine
        // ensures it won't try to update UI state after completion, and the mutex
        // will be released when the JNI call finishes naturally.
        analysisJob?.cancel()

        _uiState.value = AiAnalysisUiState.Loading
        _showBottomSheet.value = true

        analysisJob = viewModelScope.launch {
            try {
                val result = analyzeCommandUseCase(command)
                _uiState.value = AiAnalysisUiState.Success(result)
            } catch (e: AnalyzeCommandUseCase.ModelNotInstalledException) {
                _uiState.value = AiAnalysisUiState.ModelNotInstalled
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Coroutine was cancelled (user dismissed or started new analysis).
                // Don't update UI state — the new analysis or Idle state takes over.
                throw e
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
     * Cancels any running analysis to free the inference engine.
     */
    fun dismiss() {
        analysisJob?.cancel()
        analysisJob = null
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
