package `in`.hridayan.ashell.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.domain.repository.DownloadRepository
import `in`.hridayan.ashell.core.domain.usecase.DownloadApkUseCase

@Module
@InstallIn(SingletonComponent::class)
object CoreUseCaseModule {
    @Provides
    fun provideDownloadApkUseCase(repo: DownloadRepository): DownloadApkUseCase =
        DownloadApkUseCase(repo)
}
