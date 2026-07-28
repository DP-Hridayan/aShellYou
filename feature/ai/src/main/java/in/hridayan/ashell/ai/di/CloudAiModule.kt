package `in`.hridayan.ashell.ai.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.data.remote.GeminiProviderClient
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.ai.data.repository.CloudAnalysisRepositoryImpl
import `in`.hridayan.ashell.ai.data.security.ApiKeyRepositoryImpl
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.AnalyzeCommandWithCloudUseCase
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudAiModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 8000L
            socketTimeoutMillis = 12000L
            requestTimeoutMillis = 60000L
        }
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        @ApplicationContext context: Context,
    ): ApiKeyRepository = ApiKeyRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideLlmProviderClientMap(
        gemini: GeminiProviderClient,
    ): Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient> = mapOf<LlmProvider, LlmProviderClient>(
        LlmProvider.Gemini to gemini,
    )

    @Provides
    @Singleton
    fun provideCloudAnalysisRepository(
        clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
        apiKeyRepository: ApiKeyRepository,
        settingsRepository: SettingsRepository,
    ): CloudAnalysisRepository = CloudAnalysisRepositoryImpl(clients, apiKeyRepository, settingsRepository)

    @Provides
    fun provideAnalyzeCommandWithCloudUseCase(
        cloudRepository: CloudAnalysisRepository,
        commandRepository: CommandRepository,
    ): AnalyzeCommandWithCloudUseCase = AnalyzeCommandWithCloudUseCase(cloudRepository, commandRepository)
}

