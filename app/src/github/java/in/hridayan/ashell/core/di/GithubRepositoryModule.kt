package `in`.hridayan.ashell.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.repository.GoogleAuthRepositoryImpl
import `in`.hridayan.ashell.settings.data.repository.GoogleDriveRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.GoogleDriveRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GithubRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthRepository(
        googleAuthRepositoryImpl: GoogleAuthRepositoryImpl
    ): GoogleAuthRepository

    @Binds
    @Singleton
    abstract fun bindGoogleDriveRepository(
        googleDriveRepositoryImpl: GoogleDriveRepositoryImpl
    ): GoogleDriveRepository
}
