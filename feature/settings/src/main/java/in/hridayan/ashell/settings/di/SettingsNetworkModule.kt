package `in`.hridayan.ashell.settings.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import `in`.hridayan.ashell.settings.data.repository.DownloadRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.DownloadRepository
import `in`.hridayan.ashell.settings.di.qualifiers.ApiHttpClient

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object SettingsNetworkModule {

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
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
    ): DownloadRepository = DownloadRepositoryImpl(context)
}
