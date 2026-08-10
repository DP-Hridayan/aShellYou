package `in`.hridayan.ashell.ai.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult

sealed interface CloudAnalysisUiState {
    data object Idle : CloudAnalysisUiState
    data object Loading : CloudAnalysisUiState
    data class Success(val result: AnalysisResult) : CloudAnalysisUiState
    data class Error(val exception: CloudNetworkException) : CloudAnalysisUiState
}
