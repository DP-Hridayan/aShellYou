package `in`.hridayan.ashell.ai.data.repository

import `in`.hridayan.ashell.ai.data.parser.AiResponseParser
import `in`.hridayan.ashell.ai.data.parser.PromptBuilder
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ModelFallbackExecutor
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudAnalysisRepositoryImpl @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val modelCatalog: LlmModelCatalog,
    private val fallbackExecutor: ModelFallbackExecutor,
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
) : CloudAnalysisRepository {

    override suspend fun analyzeCommand(
        command: String,
        ragContext: String,
        fallbackModels: List<String>?
    ): AnalysisResult {
        val session = openSession(fallbackModels, toolsRequired = false)

        val systemPrompt = PromptBuilder.buildSystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildUserPrompt(command, ragContext)

        return fallbackExecutor.execute(session.provider, session.models) { model ->
            AiResponseParser.parse(
                session.client.complete(model, systemPrompt, userPrompt, session.apiKey)
            )
        }
    }

    override suspend fun queryCommand(
        query: String,
        tools: List<AiTool>,
        fallbackModels: List<String>?
    ): AnalysisResult {
        val session = openSession(fallbackModels, toolsRequired = tools.isNotEmpty())

        val systemPrompt = PromptBuilder.buildQuerySystemPrompt(Locale.getDefault().displayLanguage)
        val userPrompt = PromptBuilder.buildQueryUserPrompt(query, "")

        return fallbackExecutor.execute(session.provider, session.models) { model ->
            AiResponseParser.parse(
                session.client.completeWithTools(
                    model,
                    systemPrompt,
                    userPrompt,
                    session.apiKey,
                    tools
                )
            )
        }
    }

    private suspend fun openSession(
        fallbackModels: List<String>?,
        toolsRequired: Boolean,
    ): CloudSession {
        val provider = getActiveLlmProvider() ?: throw CloudNetworkException.NoActiveProvider()
        val client = clients[provider]
        val apiKey = apiKeyRepository.getKey(provider)?.takeIf { it.isNotBlank() }
        val models = fallbackModels?.takeIf { it.isNotEmpty() }
            ?: modelCatalog.chain(provider, ModelTier.LITE, toolsRequired)

        if (client == null || apiKey == null || models.isEmpty()) {
            throw CloudNetworkException.ProviderNotConfigured(provider)
        }

        return CloudSession(provider, client, apiKey, models)
    }

    private data class CloudSession(
        val provider: LlmProvider,
        val client: LlmProviderClient,
        val apiKey: String,
        val models: List<String>,
    )
}
