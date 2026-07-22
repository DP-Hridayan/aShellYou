package `in`.hridayan.ashell.settings.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.local.database.GithubRepoStatsDao
import `in`.hridayan.ashell.settings.data.remote.api.GithubApi
import `in`.hridayan.ashell.settings.data.repository.BackupAndRestoreRepositoryImpl
import `in`.hridayan.ashell.settings.data.repository.GithubDataRepositoryImpl
import `in`.hridayan.ashell.settings.di.qualifiers.ApiHttpClient
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import `in`.hridayan.ashell.settings.domain.repository.GithubDataRepository
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBackupAndRestoreRepository(
        backupAndRestoreRepositoryImpl: BackupAndRestoreRepositoryImpl
    ): BackupAndRestoreRepository

    companion object {
        @Provides
        @Singleton
        fun provideGithubApi(@ApiHttpClient client: HttpClient): GithubApi = GithubApi(client)

        @Provides
        @Singleton
        fun provideGithubDataRepository(
            api: GithubApi,
            dao: GithubRepoStatsDao
        ): GithubDataRepository =
            GithubDataRepositoryImpl(api, dao)
    }
}
