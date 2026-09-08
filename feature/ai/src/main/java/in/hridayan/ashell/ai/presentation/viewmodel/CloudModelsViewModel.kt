package `in`.hridayan.ashell.ai.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
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
    private val verifyApiKeyUseCase: VerifyApiKeyUseCase,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

    fun hasKey(provider: LlmProvider): Flow<Boolean> {
        return apiKeyRepository.hasKey(provider)
    }

    fun saveApiKey(provider: LlmProvider, key: String) {
        if (key.isNotBlank()) apiKeyRepository.setKey(provider, key)
    }

    fun deleteApiKey(provider: LlmProvider) {
        apiKeyRepository.deleteKey(provider)
        _verificationState.value = VerificationState.Idle
    }

    fun verifyKey(provider: LlmProvider) {
        _verificationState.value = VerificationState.Loading
        viewModelScope.launch {
            try {
                val key = apiKeyRepository.getKey(provider)
                    ?: throw CloudNetworkException.ProviderNotConfigured(provider)
                verifyApiKeyUseCase(provider, key)
                _verificationState.value =
                    VerificationState.Success(appContext.getString(R.string.key_verified_success_msg))
            } catch (e: CloudNetworkException) {
                _verificationState.value =
                    VerificationState.Error(appContext.getString(R.string.verification_failed) + ": ${e.message}")
            } catch (e: Exception) {
                _verificationState.value =
                    VerificationState.Error(appContext.getString(R.string.unexpected_error) + ": ${e.message}")
            }
        }
    }
}
