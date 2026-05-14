package `in`.hridayan.ashell.settings.presentation.page.licenses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.settings.domain.model.LibraryItem
import `in`.hridayan.ashell.settings.domain.model.LicensesUiState
import `in`.hridayan.ashell.settings.domain.usecase.GetLicensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    private val getLicensesUseCase: GetLicensesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicensesUiState())
    val uiState: StateFlow<LicensesUiState> = _uiState.asStateFlow()

    init {
        loadLibraries()
    }

    private fun loadLibraries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getLicensesUseCase() }
                .onSuccess { libs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            libraries = libs,
                            filteredLibraries = libs.applyQuery(state.searchQuery),
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message)
                    }
                }
        }
    }

    /** Called whenever the search field text changes. */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredLibraries = state.libraries.applyQuery(query),
            )
        }
    }

    /** Opens the detail dialog for [library]. */
    fun onLibrarySelected(library: LibraryItem) {
        _uiState.update { it.copy(selectedLibrary = library) }
    }

    /** Dismisses the detail dialog. */
    fun onDismissDetail() {
        _uiState.update { it.copy(selectedLibrary = null) }
    }

    private fun List<LibraryItem>.applyQuery(query: String): List<LibraryItem> {
        if (query.isBlank()) return this
        val lower = query.trim().lowercase()
        return filter { lib ->
            lib.name.lowercase().contains(lower) ||
                    lib.artifactId.lowercase().contains(lower) ||
                    lib.licenseName?.lowercase()?.contains(lower) == true ||
                    lib.developers.any { dev -> dev.lowercase().contains(lower) }
        }
    }
}
