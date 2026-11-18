package `in`.hridayan.ashell.settings.presentation.page.logsandconfiguration.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.settings.domain.usecase.BuildDiagnosticConfigUseCase
import `in`.hridayan.ashell.settings.domain.usecase.ExportDiagnosticConfigUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LogsAndConfigurationUiState(
    val configPreview: String = "",
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
)

@HiltViewModel
class LogsAndConfigurationViewModel @Inject constructor(
    private val buildDiagnosticConfigUseCase: BuildDiagnosticConfigUseCase,
    private val exportDiagnosticConfigUseCase: ExportDiagnosticConfigUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsAndConfigurationUiState())
    val uiState: StateFlow<LogsAndConfigurationUiState> = _uiState.asStateFlow()

    private val _showExportSheet = MutableStateFlow(false)
    val showExportSheet: StateFlow<Boolean> = _showExportSheet.asStateFlow()

    private val _shareUriEvent = MutableSharedFlow<Uri>()
    val shareUriEvent: SharedFlow<Uri> = _shareUriEvent.asSharedFlow()

    fun openExportSheet() {
        _showExportSheet.value = true
        loadConfigPreview()
    }

    fun dismissExportSheet() {
        _showExportSheet.value = false
    }

    fun exportConfig() {
        val currentPreview = _uiState.value.configPreview
        if (currentPreview.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val result = withContext(Dispatchers.IO) {
                exportDiagnosticConfigUseCase(currentPreview)
            }

            _uiState.update { it.copy(isExporting = false) }

            result.onSuccess { uri ->
                _showExportSheet.value = false
                _shareUriEvent.emit(uri)
            }
        }
    }

    private fun loadConfigPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val report = withContext(Dispatchers.IO) {
                buildDiagnosticConfigUseCase().formatReport()
            }

            _uiState.update {
                it.copy(
                    configPreview = report,
                    isLoading = false,
                )
            }
        }
    }
}
