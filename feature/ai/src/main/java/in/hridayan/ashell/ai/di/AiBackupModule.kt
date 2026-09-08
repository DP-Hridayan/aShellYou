package `in`.hridayan.ashell.ai.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.ai.data.backup.AiBackupProvider
import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBackupModule {
    @Binds
    @IntoSet
    abstract fun bindAiBackupProvider(provider: AiBackupProvider): BackupProvider
}
