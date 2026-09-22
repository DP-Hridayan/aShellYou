package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Whether an AI feature can run right now, and why not when it cannot. */
sealed interface ActiveProviderKeyStatus {
    /** Ready to go: [provider] is active and has a key. */
    data class Allowed(val provider: LlmProvider) : ActiveProviderKeyStatus

    /** The user has not picked a provider yet. */
    data object NoProviderSelected : ActiveProviderKeyStatus

    /** [provider] is active but holds no key, so the user must add one or switch providers. */
    data class NoKeyFor(val provider: LlmProvider) : ActiveProviderKeyStatus
}

/**
 * The single gate every AI entry point checks before doing work.
 *
 * Distinguishing "nothing chosen" from "chosen but unusable" is what lets the UI offer the right
 * remedy: adding a first key versus switching to a provider that already has one.
 */
@Singleton
class RequireActiveProviderKeyUseCase @Inject constructor(
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
    private val apiKeyRepository: ApiKeyRepository,
) {
    suspend operator fun invoke(): ActiveProviderKeyStatus {
        val provider = getActiveLlmProvider() ?: return ActiveProviderKeyStatus.NoProviderSelected

        return if (apiKeyRepository.getKey(provider).isNullOrBlank()) {
            ActiveProviderKeyStatus.NoKeyFor(provider)
        } else {
            ActiveProviderKeyStatus.Allowed(provider)
        }
    }
}
