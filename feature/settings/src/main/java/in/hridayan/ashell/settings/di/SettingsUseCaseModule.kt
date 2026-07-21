package `in`.hridayan.ashell.settings.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.domain.repository.DownloadRepository
import `in`.hridayan.ashell.settings.domain.usecase.DownloadApkUseCase
import `in`.hridayan.ashell.settings.domain.usecase.GetAllChangelogsUseCase

@Module
@InstallIn(SingletonComponent::class)
object SettingsUseCaseModule {
    @Provides
    fun provideGetChangelogsUseCase(@ApplicationContext context: Context): GetAllChangelogsUseCase =
        GetAllChangelogsUseCase(context)

    @Provides
    fun provideDownloadApkUseCase(repo: DownloadRepository): DownloadApkUseCase =
        DownloadApkUseCase(repo)
}
