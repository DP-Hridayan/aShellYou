package `in`.hridayan.ashell.ai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.DangerLevel
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StubAiModule {

    @Provides
    @Singleton
    fun provideAiAnalysisRepository(): AiAnalysisRepository = object : AiAnalysisRepository {
        override suspend fun analyzeCommand(
            command: String,
            ragContext: String,
            fallbackModels: List<String>?
        ): AnalysisResult = AnalysisResult.error("AI features are disabled in this flavor.")

        override suspend fun queryCommand(
            query: String,
            tools: List<AiTool>,
            fallbackModels: List<String>?
        ): AnalysisResult = AnalysisResult.error("AI features are disabled in this flavor.")

        override suspend fun generateRawCompletion(prompt: String, maxTokens: Int): String = ""

        override suspend fun getCachedAnalysis(command: String): AnalysisResult? = null

        override suspend fun clearCache() {}

        override suspend fun getCacheSizeBytes(): Long = 0L
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(): ApiKeyRepository = object : ApiKeyRepository {
        override fun setKey(provider: LlmProvider, key: String) {}
        override fun getKey(provider: LlmProvider): String? = null
        override fun deleteKey(provider: LlmProvider) {}
        override fun hasKey(provider: LlmProvider): Flow<Boolean> = flowOf(false)
    }

    @Provides
    fun provideLlmProviderClients(): Map<LlmProvider, LlmProviderClient> = emptyMap()
}
