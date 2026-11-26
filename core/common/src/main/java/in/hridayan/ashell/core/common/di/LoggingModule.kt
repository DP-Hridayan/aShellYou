package `in`.hridayan.ashell.core.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.logging.AndroidAppLogger
import `in`.hridayan.ashell.core.common.logging.AppLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LoggingModule {

    @Binds
    @Singleton
    fun bindAppLogger(impl: AndroidAppLogger): AppLogger
}
