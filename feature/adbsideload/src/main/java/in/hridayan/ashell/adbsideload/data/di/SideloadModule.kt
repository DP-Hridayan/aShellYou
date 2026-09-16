package `in`.hridayan.ashell.adbsideload.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.adbsideload.data.repository.SideloadRepositoryImpl
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SideloadModule {

    @Binds
    @Singleton
    abstract fun bindSideloadRepository(impl: SideloadRepositoryImpl): SideloadRepository
}
