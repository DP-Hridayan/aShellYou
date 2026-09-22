package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the provider the user has chosen to use for AI features.
 *
 * `null` means "None": either nothing has been chosen yet, or the stored identifier belongs to a
 * provider the app no longer ships.
 */
@Singleton
class GetActiveLlmProviderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    fun asFlow(): Flow<LlmProvider?> =
        settingsRepository.getString(SettingsKeys.AiCloudProvider).map(::resolve)

    suspend operator fun invoke(): LlmProvider? =
        resolve(settingsRepository.getString(SettingsKeys.AiCloudProvider).first())

    private fun resolve(providerId: String): LlmProvider? =
        providerId.takeIf { it.isNotBlank() }?.let(LlmProvider::fromId)
}
