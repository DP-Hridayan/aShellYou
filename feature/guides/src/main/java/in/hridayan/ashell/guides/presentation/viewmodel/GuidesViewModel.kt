package `in`.hridayan.ashell.guides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.guides.domain.usecase.GetGuidesUseCase
import `in`.hridayan.ashell.guides.presentation.model.GuidesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GuidesViewModel @Inject constructor(
    private val getGuidesUseCase: GetGuidesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GuidesUiState(groupedGuides = getGuidesUseCase())
    )
    val uiState: StateFlow<GuidesUiState> = _uiState.asStateFlow()
}
