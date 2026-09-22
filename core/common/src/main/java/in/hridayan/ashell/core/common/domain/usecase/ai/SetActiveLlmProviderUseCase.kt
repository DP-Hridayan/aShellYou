package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import javax.inject.Inject
import javax.inject.Singleton

/** Result of attempting to change the active provider. */
sealed interface SetActiveProviderOutcome {
    data object Saved : SetActiveProviderOutcome

    /** Refused: [provider] has no API key stored, so selecting it would break every AI feature. */
    data class NoKeyFor(val provider: LlmProvider) : SetActiveProviderOutcome
}

/**
 * Persists the user's choice of active provider, refusing any provider without a stored API key.
 *
 * Returns an outcome rather than throwing so the picker dialog can show an inline error and stay
 * open without the domain layer reaching into the UI.
 */
@Singleton
class SetActiveLlmProviderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val apiKeyRepository: ApiKeyRepository,
) {
    /** @param provider The provider to activate, or `null` to select "None". */
    suspend operator fun invoke(provider: LlmProvider?): SetActiveProviderOutcome {
        if (provider == null) {
            settingsRepository.setString(SettingsKeys.AiCloudProvider, NO_PROVIDER)
            return SetActiveProviderOutcome.Saved
        }

        if (apiKeyRepository.getKey(provider).isNullOrBlank()) {
            return SetActiveProviderOutcome.NoKeyFor(provider)
        }

        settingsRepository.setString(SettingsKeys.AiCloudProvider, provider.id)
        return SetActiveProviderOutcome.Saved
    }

    companion object {
        const val NO_PROVIDER = ""
    }
}
