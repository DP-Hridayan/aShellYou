package `in`.hridayan.ashell.core.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.SetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.SetActiveProviderOutcome
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the pending selection while the provider picker is open.
 *
 * @param selected The provider the user has highlighted, or `null` for "None".
 * @param error Why the last confirmation was refused, or `null` when there is nothing to report.
 */
data class ActiveProviderDialogState(
    val selected: LlmProvider? = null,
    val error: String? = null,
)

/**
 * Backs the provider picker for every feature that can raise it.
 *
 * Lives in `core` rather than in the AI feature because the picker is reachable from the shell,
 * command examples and settings, and feature modules must not depend on one another.
 */
@HiltViewModel
class ActiveProviderViewModel @Inject constructor(
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
    private val setActiveLlmProvider: SetActiveLlmProviderUseCase,
    @param:ApplicationContext private val appContext: Context,
) : ViewModel() {

    val activeProvider: StateFlow<LlmProvider?> = getActiveLlmProvider.asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), null)

    private val _dialogState = MutableStateFlow(ActiveProviderDialogState())
    val dialogState: StateFlow<ActiveProviderDialogState> = _dialogState.asStateFlow()

    val providers: List<LlmProvider> = LlmProvider.all

    /** Seeds the pending selection from what is currently active. */
    fun prepare() {
        viewModelScope.launch {
            _dialogState.value = ActiveProviderDialogState(selected = getActiveLlmProvider())
        }
    }

    fun select(provider: LlmProvider?) {
        _dialogState.value = ActiveProviderDialogState(selected = provider)
    }

    /** @param onSaved Invoked only when the selection was accepted and persisted. */
    fun confirm(onSaved: () -> Unit) {
        viewModelScope.launch {
            when (val outcome = setActiveLlmProvider(_dialogState.value.selected)) {
                is SetActiveProviderOutcome.Saved -> onSaved()

                is SetActiveProviderOutcome.NoKeyFor -> {
                    _dialogState.value = _dialogState.value.copy(
                        error = appContext.getString(
                            R.string.error_no_api_key_for_provider,
                            outcome.provider.displayName
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
}
