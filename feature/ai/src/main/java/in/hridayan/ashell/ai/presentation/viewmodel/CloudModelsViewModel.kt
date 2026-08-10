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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudModelsViewModel @Inject constructor(
    val apiKeyRepository: ApiKeyRepository,
    private val verifyApiKeyUseCase: VerifyApiKeyUseCase,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _verificationResult = MutableStateFlow<String?>(null)
    val verificationResult: StateFlow<String?> = _verificationResult.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    fun saveApiKey(provider: LlmProvider, key: String) {
        if (key.isNotBlank()) apiKeyRepository.setKey(provider, key)
    }

    fun deleteApiKey(provider: LlmProvider) {
        apiKeyRepository.deleteKey(provider)
        _verificationResult.value = null
    }

    fun verifyKey(provider: LlmProvider) {
        _isVerifying.value = true
        _verificationResult.value = null
        viewModelScope.launch {
            try {
                val key = apiKeyRepository.getKey(provider)
                    ?: throw CloudNetworkException.ProviderNotConfigured(provider)
                verifyApiKeyUseCase(provider, key)
                _verificationResult.value =
                    "✅ " + appContext.getString(R.string.key_verified_success_msg)
            } catch (e: CloudNetworkException) {
                _verificationResult.value =
                    "❌ " + appContext.getString(R.string.verification_failed) + ": ${e.message}"
            } catch (e: Exception) {
                _verificationResult.value =
                    "❌ " + appContext.getString(R.string.unexpected_error) + ": ${e.message}"
            } finally {
                _isVerifying.value = false
            }
        }
    }
}
