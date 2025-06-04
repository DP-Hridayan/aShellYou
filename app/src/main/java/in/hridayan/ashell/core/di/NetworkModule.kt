package `in`.hridayan.ashell.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.data.repository.DownloadRepositoryImpl
import `in`.hridayan.ashell.core.di.qualifiers.ApiHttpClient
import `in`.hridayan.ashell.core.domain.repository.DownloadRepository
import `in`.hridayan.ashell.settings.data.remote.api.GitHubApi
import `in`.hridayan.ashell.settings.data.remote.repository.UpdateRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.UpdateRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ApiHttpClient
    fun provideApiHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Provides
    @Singleton
    fun provideGitHubApi(@ApiHttpClient client: HttpClient): GitHubApi = GitHubApi(client)

    @Provides
    @Singleton
    fun provideUpdateRepository(api: GitHubApi): UpdateRepository = UpdateRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
    ): DownloadRepository = DownloadRepositoryImpl(context)
}