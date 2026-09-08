package `in`.hridayan.ashell.core.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider
import `in`.hridayan.ashell.core.presentation.theme.data.backup.ColorSchemeBackupProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class ColorSchemeBackupModule {
    @Binds
    @IntoSet
    abstract fun bindColorSchemeBackupProvider(provider: ColorSchemeBackupProvider): BackupProvider
}
