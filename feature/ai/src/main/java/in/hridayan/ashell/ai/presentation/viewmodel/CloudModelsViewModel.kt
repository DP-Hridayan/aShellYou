package `in`.hridayan.ashell.ai.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.presentation.util.toUserMessage
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.DeleteApiKeyUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.SaveApiKeyUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.VerifyApiKeyUseCase
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VerificationState {
    data object Idle : VerificationState
    data object Loading : VerificationState
    data class Success(val message: String) : VerificationState
    data class Error(val message: String) : VerificationState
}

@HiltViewModel
class CloudModelsViewModel @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository,
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val deleteApiKeyUseCase: DeleteApiKeyUseCase,
    private val verifyApiKeyUseCase: VerifyApiKeyUseCase,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    /**
     * Verification outcomes keyed by provider id.
     *
     * Kept per provider so verifying one card leaves the others alone.
     */
    private val _verificationStates = MutableStateFlow<Map<String, VerificationState>>(emptyMap())
    val verificationStates: StateFlow<Map<String, VerificationState>> =
        _verificationStates.asStateFlow()

    fun hasKey(provider: LlmProvider): Flow<Boolean> = apiKeyRepository.hasKey(provider)

    /**
     * Verifies a key against the provider and stores it only if the provider accepts it.
     *
     * Saving an unusable key is worse than refusing it: the failure would otherwise surface much
     * later, inside whichever AI feature the user tried next.
     */
    fun saveApiKey(provider: LlmProvider, key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isEmpty()) return

        setState(provider, VerificationState.Loading)

        viewModelScope.launch {
            runVerification(provider, trimmedKey) {
                saveApiKeyUseCase(provider, trimmedKey)
            }
        }
    }

    fun deleteApiKey(provider: LlmProvider) {
        viewModelScope.launch {
            deleteApiKeyUseCase(provider)
            setState(provider, VerificationState.Idle)
        }
    }

    fun verifyKey(provider: LlmProvider) {
        setState(provider, VerificationState.Loading)

        viewModelScope.launch {
            val key = apiKeyRepository.getKey(provider)?.takeIf { it.isNotBlank() }

            if (key == null) {
                setState(
                    provider,
                    VerificationState.Error(
                        CloudNetworkException.ProviderNotConfigured(provider)
                            .toUserMessage(appContext)
                    )
                )
                return@launch
            }

            runVerification(provider, key) {}
        }
    }

    /** @param onVerified Runs only after the provider has accepted the key. */
    private suspend fun runVerification(
        provider: LlmProvider,
        key: String,
        onVerified: suspend () -> Unit,
    ) {
        try {
            verifyApiKeyUseCase(provider, key)
            onVerified()
            setState(
                provider,
                VerificationState.Success(appContext.getString(R.string.key_verified_success_msg))
            )
        } catch (e: CloudNetworkException) {
            setState(provider, VerificationState.Error(e.toUserMessage(appContext)))
        } catch (e: Exception) {
            setState(
                provider,
                VerificationState.Error(
                    appContext.getString(R.string.unexpected_error) + ": ${e.message}"
                )
            )
        }
    }

    private fun setState(provider: LlmProvider, state: VerificationState) {
        _verificationStates.value = _verificationStates.value + (provider.id to state)
    }
}
