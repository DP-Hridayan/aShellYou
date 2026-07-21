package `in`.hridayan.ashell.settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.settings.data.backup.SettingsBackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsBackupModule {
    @Binds
    @IntoSet
    abstract fun bindSettingsBackupProvider(provider: SettingsBackupProvider): BackupProvider
}
