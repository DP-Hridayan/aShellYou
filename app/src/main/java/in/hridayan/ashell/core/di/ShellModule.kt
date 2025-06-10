package `in`.hridayan.ashell.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.data.repository.ShellRepositoryImpl
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.domain.usecase.ShellCommandExecutor
import `in`.hridayan.ashell.shell.domain.usecase.ShizukuPermissionHandler

@Module
@InstallIn(SingletonComponent::class)
object ShellModule {
    @Provides
    fun provideShellRepository(
        shellCommandExecutor: ShellCommandExecutor,
        shizukuPermissionHandler: ShizukuPermissionHandler,
        @ApplicationContext context: Context
    ): ShellRepository =
        ShellRepositoryImpl(shellCommandExecutor, shizukuPermissionHandler, context)
}