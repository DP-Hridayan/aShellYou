package `in`.hridayan.ashell.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.repository.FdroidGoogleAuthRepositoryImpl
import `in`.hridayan.ashell.settings.data.repository.FdroidGoogleDriveRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FdroidRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthRepository(
        fdroidGoogleAuthRepositoryImpl: FdroidGoogleAuthRepositoryImpl
    ): GoogleAuthRepository

    @Binds
    @Singleton
    abstract fun bindGoogleDriveRepository(
        fdroidGoogleDriveRepositoryImpl: FdroidGoogleDriveRepositoryImpl
    ): GoogleDriveRepository
}
