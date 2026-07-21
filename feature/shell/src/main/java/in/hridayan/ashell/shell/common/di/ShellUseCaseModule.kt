package `in`.hridayan.ashell.shell.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.common.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.GetSaveOutputFileNameUseCase
import `in`.hridayan.ashell.shell.local_adb_shell.data.shell.ShellCommandExecutor
import `in`.hridayan.ashell.shell.local_adb_shell.data.shizuku.ShizukuPermissionHandler

@Module
@InstallIn(SingletonComponent::class)
object ShellUseCaseModule {
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
