package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.data.repository.FileBrowserRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.repository.FileBrowserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileBrowserModule {

    @Binds
    @Singleton
    abstract fun bindFileBrowserRepository(
        impl: FileBrowserRepositoryImpl
    ): FileBrowserRepository
}
