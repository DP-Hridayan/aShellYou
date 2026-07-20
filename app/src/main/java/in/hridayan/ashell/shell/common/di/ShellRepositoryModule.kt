package `in`.hridayan.ashell.shell.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.common.data.repository.BookmarkRepositoryImpl
import `in`.hridayan.ashell.shell.common.data.repository.PackageRepositoryImpl
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShellRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindPackageRepository(
        packageRepositoryImpl: PackageRepositoryImpl
    ): PackageRepository
}
