package `in`.hridayan.ashell.commandexamples.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.commandexamples.data.backup.CommandBackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class CommandBackupModule {
    @Binds
    @IntoSet
    abstract fun bindCommandBackupProvider(provider: CommandBackupProvider): BackupProvider
}
