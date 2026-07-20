package `in`.hridayan.ashell.crashreporter.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.crashreporter.data.repository.CrashRepositoryImpl
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCrashRepository(
        crashRepositoryImpl: CrashRepositoryImpl
    ): CrashRepository
}
