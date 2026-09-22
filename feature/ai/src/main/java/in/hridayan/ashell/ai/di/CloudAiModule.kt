package `in`.hridayan.ashell.ai.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.ai.data.remote.GeminiProviderClient
import `in`.hridayan.ashell.ai.data.remote.catalog.LlmModelCatalogImpl
import `in`.hridayan.ashell.ai.data.remote.openai.OpenAiCompatibleConfig
import `in`.hridayan.ashell.ai.data.remote.openai.OpenAiCompatibleProviderClient
import `in`.hridayan.ashell.ai.data.repository.CloudAnalysisRepositoryImpl
import `in`.hridayan.ashell.ai.data.security.ApiKeyRepositoryImpl
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.AnalyzeCommandWithCloudUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ModelFallbackExecutor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private const val CONNECT_TIMEOUT_MILLIS = 8000L
private const val SOCKET_TIMEOUT_MILLIS = 12000L
private const val REQUEST_TIMEOUT_MILLIS = 60000L

@Module
@InstallIn(SingletonComponent::class)
object CloudAiModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true

                    // Providers validate tool payloads strictly. Defaults must be written out or a
                    // tool arrives without its required `type`, and nulls must be omitted or an
                    // absent schema member arrives as an explicit null, which is also rejected.
                    encodeDefaults = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        }
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        @ApplicationContext context: Context,
    ): ApiKeyRepository = ApiKeyRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideLlmModelCatalog(impl: LlmModelCatalogImpl): LlmModelCatalog = impl

    @Provides
    @Singleton
    fun provideLlmProviderClientMap(
        gemini: GeminiProviderClient,
        httpClient: HttpClient,
    ): Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient> =
        mapOf<LlmProvider, LlmProviderClient>(
            LlmProvider.Gemini to gemini,
            LlmProvider.Groq to OpenAiCompatibleProviderClient(
                httpClient,
                OpenAiCompatibleConfig.Groq
            ),
            LlmProvider.OpenRouter to OpenAiCompatibleProviderClient(
                httpClient,
                OpenAiCompatibleConfig.OpenRouter
            ),
        )

    @Provides
    @Singleton
    fun provideCloudAnalysisRepository(
        clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
        apiKeyRepository: ApiKeyRepository,
        modelCatalog: LlmModelCatalog,
        fallbackExecutor: ModelFallbackExecutor,
        getActiveLlmProvider: GetActiveLlmProviderUseCase,
    ): CloudAnalysisRepository = CloudAnalysisRepositoryImpl(
        clients = clients,
        apiKeyRepository = apiKeyRepository,
        modelCatalog = modelCatalog,
        fallbackExecutor = fallbackExecutor,
        getActiveLlmProvider = getActiveLlmProvider,
    )

    @Provides
    fun provideAnalyzeCommandWithCloudUseCase(
        cloudRepository: CloudAnalysisRepository,
        commandRepository: CommandRepository,
    ): AnalyzeCommandWithCloudUseCase = AnalyzeCommandWithCloudUseCase(cloudRepository, commandRepository)
}
