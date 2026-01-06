package `in`.hridayan.ashell.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.domain.repository.DownloadRepository
import `in`.hridayan.ashell.core.domain.usecase.DownloadApkUseCase
import `in`.hridayan.ashell.settings.domain.usecase.GetAllChangelogsUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.GetSaveOutputFileNameUseCase
import `in`.hridayan.ashell.shell.local_adb_shell.data.shell.ShellCommandExecutor
import `in`.hridayan.ashell.shell.local_adb_shell.data.shizuku.ShizukuPermissionHandler

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetChangelogsUseCase(@ApplicationContext context: Context): GetAllChangelogsUseCase =
        GetAllChangelogsUseCase(context)

    @Provides
    fun provideDownloadApkUseCase(repo: DownloadRepository): DownloadApkUseCase =
        DownloadApkUseCase(repo)

    @Provides
    fun provideShellCommandExecutor(): ShellCommandExecutor = ShellCommandExecutor()

    @Provides
    fun provideShizukuPermissionHandler(): ShizukuPermissionHandler = ShizukuPermissionHandler()

    @Provides
    fun provideExtractLastCommandOutputUseCase(): ExtractLastCommandOutputUseCase =
        ExtractLastCommandOutputUseCase()

    @Provides
    fun provideGetSaveOutputFileNameUseCase(): GetSaveOutputFileNameUseCase =
        GetSaveOutputFileNameUseCase()
}