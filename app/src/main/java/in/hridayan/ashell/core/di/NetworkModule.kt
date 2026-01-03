package `in`.hridayan.ashell.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.data.local.database.GithubRepoStatsDao
import `in`.hridayan.ashell.core.data.local.repository.DownloadRepositoryImpl
import `in`.hridayan.ashell.core.data.remote.api.GithubApi
import `in`.hridayan.ashell.core.data.remote.repository.GithubDataRepositoryImpl
import `in`.hridayan.ashell.core.di.qualifiers.ApiHttpClient
import `in`.hridayan.ashell.core.domain.repository.DownloadRepository
import `in`.hridayan.ashell.core.domain.repository.GithubDataRepository
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
    fun provideGithubApi(@ApiHttpClient client: HttpClient): GithubApi = GithubApi(client)

    @Provides
    @Singleton
    fun provideGithubDataRepository(api: GithubApi, dao: GithubRepoStatsDao): GithubDataRepository =
        GithubDataRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
    ): DownloadRepository = DownloadRepositoryImpl(context)
}