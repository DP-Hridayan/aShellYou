package `in`.hridayan.ashell.shell.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.shell.common.data.backup.BookmarkBackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class BookmarkBackupModule {
    @Binds
    @IntoSet
    abstract fun bindBookmarkBackupProvider(provider: BookmarkBackupProvider): BackupProvider
}
