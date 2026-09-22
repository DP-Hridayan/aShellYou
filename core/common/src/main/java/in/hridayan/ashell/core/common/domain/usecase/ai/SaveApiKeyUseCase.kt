package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores an API key and adopts its provider when it is the only one configured.
 *
 * Adoption means a user who saves their first key never has to also discover the provider picker.
 * Once a second provider holds a key, saving a key no longer changes the active provider, so an
 * explicit choice is never silently overridden.
 */
@Singleton
class SaveApiKeyUseCase @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(provider: LlmProvider, key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isEmpty()) return

        apiKeyRepository.setKey(provider, trimmedKey)

        if (isOnlyConfiguredProvider(provider)) {
            settingsRepository.setString(SettingsKeys.AiCloudProvider, provider.id)
        }
    }

    private fun isOnlyConfiguredProvider(provider: LlmProvider): Boolean =
        LlmProvider.all.none { it != provider && !apiKeyRepository.getKey(it).isNullOrBlank() }
}
