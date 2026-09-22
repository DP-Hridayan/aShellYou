package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Removes an API key and keeps the active provider pointing at something usable.
 *
 * Deleting the active provider's key falls through [LlmProvider.all] to the next provider that
 * still holds one, so a user with several keys configured is never dropped into a broken state.
 * When no key remains anywhere, the active provider becomes "None".
 */
@Singleton
class DeleteApiKeyUseCase @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository,
    private val settingsRepository: SettingsRepository,
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
) {
    suspend operator fun invoke(provider: LlmProvider) {
        val wasActive = getActiveLlmProvider() == provider

        apiKeyRepository.deleteKey(provider)

        if (!wasActive) return

        val successor = LlmProvider.all.firstOrNull { !apiKeyRepository.getKey(it).isNullOrBlank() }
        settingsRepository.setString(
            SettingsKeys.AiCloudProvider,
            successor?.id ?: SetActiveLlmProviderUseCase.NO_PROVIDER
        )
    }
}
