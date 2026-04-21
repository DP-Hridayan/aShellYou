package `in`.hridayan.ashell.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.commandexamples.data.local.repository.CommandRepositoryImpl
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.shell.common.data.repository.BookmarkRepositoryImpl
import `in`.hridayan.ashell.shell.common.data.repository.PackageRepositoryImpl
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import `in`.hridayan.ashell.crashreporter.data.repository.CrashRepositoryImpl
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import `in`.hridayan.ashell.settings.data.repository.BackupAndRestoreRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.BackupAndRestoreRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCommandRepository(
        commandRepositoryImpl: CommandRepositoryImpl
    ): CommandRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindBackupAndRestoreRepository(
        backupAndRestoreRepositoryImpl: BackupAndRestoreRepositoryImpl
    ): BackupAndRestoreRepository

    @Binds
    @Singleton
    abstract fun bindCrashRepository(
        crashRepositoryImpl: CrashRepositoryImpl
    ): CrashRepository

    @Binds
    @Singleton
    abstract fun bindPackageRepository(
        packageRepositoryImpl: PackageRepositoryImpl
    ): PackageRepository
}
