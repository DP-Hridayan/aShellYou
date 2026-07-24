package `in`.hridayan.ashell.logcat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.logcat.data.repository.LogcatFilterRepositoryImpl
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LogcatModule {

    @Provides
    @Singleton
    fun provideLogcatFilterRepository(
        @ApplicationContext context: Context
    ): LogcatFilterRepository = LogcatFilterRepositoryImpl(context)
}
