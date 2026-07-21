package `in`.hridayan.ashell.qstiles.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.qstiles.data.backup.TileBackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class TileBackupModule {

    @Binds
    @IntoSet
    abstract fun bindTileBackupProvider(
        provider: TileBackupProvider
    ): BackupProvider
}
