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

    override suspend fun analyzeCommand(command: String, ragContext: String, fallbackModels: List<String>?): AnalysisResult {
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        val provider = LlmProvider.fromId(providerId) ?: LlmProvider.Gemini
        val client = clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)
        val apiKey = apiKeyRepository.getKey(provider)
            ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        val models = fallbackModels?.takeIf { it.isNotEmpty() } ?: AiModelConstants.geminiLiteModels
        if (models.isEmpty()) throw CloudNetworkException.ProviderNotConfigured(provider)

        val systemPrompt = PromptBuilder.buildSystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildUserPrompt(command, ragContext)

        var lastException: CloudNetworkException? = null
        for (model in models) {
            try {
                val rawResponse = client.complete(model, systemPrompt, userPrompt, apiKey)
                return AiResponseParser.parse(rawResponse)
            } catch (e: CloudNetworkException.RateLimited) {
                lastException = e
                // Continue to the next model in the fallback chain
            } catch (e: CloudNetworkException.ServerError) {
                if (e.code == 429 || e.code >= 500) {
                    lastException = e
                    // Continue to the next model
                } else {
                    throw e
                }
            } catch (e: CloudNetworkException.NetworkError) {
                lastException = e
                // Continue to the next model
            } catch (e: CloudNetworkException.ParseError) {
                lastException = e
                // Continue to the next model
            } catch (e: CloudNetworkException) {
                // For Auth errors, don't retry, just throw immediately since it's fatal
                throw e
            }
        }

        // If we exhausted the entire chain, throw the last exception encountered
        // If we exhausted the entire chain, throw the last exception encountered
        throw lastException ?: CloudNetworkException.ProviderNotConfigured(provider)
    }

    override suspend fun queryCommand(query: String, tools: List<AiTool>, fallbackModels: List<String>?): AnalysisResult {
        val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull()
            ?: SettingsKeys.AiCloudProvider.default
        val provider = LlmProvider.fromId(providerId) ?: LlmProvider.Gemini
        val client = clients[provider] ?: throw CloudNetworkException.ProviderNotConfigured(provider)
        val apiKey = apiKeyRepository.getKey(provider)
            ?: throw CloudNetworkException.ProviderNotConfigured(provider)

        val models = fallbackModels?.takeIf { it.isNotEmpty() } ?: AiModelConstants.geminiLiteModels
        if (models.isEmpty()) throw CloudNetworkException.ProviderNotConfigured(provider)

        val systemPrompt = PromptBuilder.buildQuerySystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildQueryUserPrompt(query, "")

        var lastException: CloudNetworkException? = null
        for (model in models) {
            try {
                val rawResponse = client.completeWithTools(model, systemPrompt, userPrompt, apiKey, tools)
                return AiResponseParser.parse(rawResponse)
            } catch (e: CloudNetworkException.RateLimited) {
                lastException = e
            } catch (e: CloudNetworkException.ServerError) {
                if (e.code == 429 || e.code >= 500) {
                    lastException = e
                } else {
                    throw e
                }
            } catch (e: CloudNetworkException.NetworkError) {
                lastException = e
            } catch (e: CloudNetworkException.ParseError) {
                lastException = e
            } catch (e: CloudNetworkException) {
                throw e
            }
        }

        throw lastException ?: CloudNetworkException.ProviderNotConfigured(provider)
    }
}
