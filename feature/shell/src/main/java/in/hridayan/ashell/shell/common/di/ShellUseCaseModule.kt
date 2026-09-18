package `in`.hridayan.ashell.shell.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import `in`.hridayan.ashell.shell.common.domain.usecase.ExtractLastCommandOutputUseCase
import `in`.hridayan.ashell.shell.common.domain.usecase.GetSaveOutputFileNameUseCase
import `in`.hridayan.ashell.shell.local_adb_shell.data.shell.ShellCommandExecutor
import `in`.hridayan.ashell.shell.local_adb_shell.data.shizuku.ShizukuPermissionHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShellUseCaseModule {
    @Provides
    fun provideShellCommandExecutor(
        @ApplicationContext context: Context,
        shizukuCommandRunner: ShizukuCommandRunner
    ): ShellCommandExecutor = ShellCommandExecutor(context, shizukuCommandRunner)

    @Provides
    @Singleton
    fun provideShizukuPermissionHandler(): ShizukuPermissionHandler = ShizukuPermissionHandler()

    @Provides
    fun provideExtractLastCommandOutputUseCase(): ExtractLastCommandOutputUseCase =
        ExtractLastCommandOutputUseCase()

    @Provides
    fun provideGetSaveOutputFileNameUseCase(): GetSaveOutputFileNameUseCase =
        GetSaveOutputFileNameUseCase()
}
