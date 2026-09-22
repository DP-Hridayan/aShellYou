package `in`.hridayan.ashell.ai.data.repository

import `in`.hridayan.ashell.ai.data.parser.AiResponseParser
import `in`.hridayan.ashell.ai.data.parser.PromptBuilder
import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAnalysisRepositoryImpl @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val settingsRepository: SettingsRepository,
) : CloudAnalysisRepository {

    override suspend fun analyzeCommand(
        command: String,
        ragContext: String,
        fallbackModels: List<String>?
    ): AnalysisResult {
        val provider = getProvider()
        val client =
            clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)
        val apiKey = apiKeyRepository.getKey(provider)
            ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        val models = fallbackModels?.takeIf { it.isNotEmpty() } ?: AiModelConstants.geminiLiteModels
        if (models.isEmpty()) throw CloudNetworkException.ProviderNotConfigured(provider)

        val systemPrompt = PromptBuilder.buildSystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildUserPrompt(command, ragContext)

        return executeWithFallback(provider, models) { model ->
            val rawResponse = client.complete(model, systemPrompt, userPrompt, apiKey)
            AiResponseParser.parse(rawResponse)
        }
    }

    override suspend fun queryCommand(
        query: String,
        tools: List<AiTool>,
        fallbackModels: List<String>?
    ): AnalysisResult {
        val provider = getProvider()
        val client =
            clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)
        val apiKey = apiKeyRepository.getKey(provider)
            ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        val models = fallbackModels?.takeIf { it.isNotEmpty() } ?: AiModelConstants.geminiLiteModels
        if (models.isEmpty()) throw CloudNetworkException.ProviderNotConfigured(provider)

        val systemPrompt = PromptBuilder.buildQuerySystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildQueryUserPrompt(query, "")

        return executeWithFallback(provider, models) { model ->
            val rawResponse = client.completeWithTools(model, systemPrompt, userPrompt, apiKey, tools)
            AiResponseParser.parse(rawResponse)
        }
    }

    private suspend fun executeWithFallback(
        provider: LlmProvider,
        models: List<String>,
        action: suspend (String) -> AnalysisResult
    ): AnalysisResult {
        var lastException: CloudNetworkException? = null
        for (model in models) {
            try {
                return action(model)
            } catch (e: CloudNetworkException) {
                if (shouldRetry(e)) {
                    lastException = e
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: CloudNetworkException.ProviderNotConfigured(provider)
    }

    private fun shouldRetry(e: CloudNetworkException): Boolean {
        return when (e) {
            is CloudNetworkException.RateLimited,
            is CloudNetworkException.NetworkError,
            is CloudNetworkException.ParseError -> true
            is CloudNetworkException.ServerError -> e.code == 429 || e.code >= 500
            else -> false
        }
    }

    private suspend fun getProvider(): LlmProvider {
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        return LlmProvider.fromId(providerId) ?: LlmProvider.Gemini
    }
}
