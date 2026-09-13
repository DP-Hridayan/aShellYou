package `in`.hridayan.ashell.qstiles.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.repository.MaterialIconRepositoryImpl
import `in`.hridayan.ashell.qstiles.domain.repository.MaterialIconRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaterialIconModule {

    @Binds
    @Singleton
    abstract fun bindMaterialIconRepository(
        impl: MaterialIconRepositoryImpl,
    ): MaterialIconRepository

    companion object {

        @Provides
        @Singleton
        @MaterialIconHttpClient
        fun provideMaterialIconHttpClient(): HttpClient = HttpClient(CIO) {
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
        }

        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val SOCKET_TIMEOUT_MS = 30_000L
        private const val REQUEST_TIMEOUT_MS = 60_000L
    }
}
