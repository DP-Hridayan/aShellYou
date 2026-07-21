package `in`.hridayan.ashell.logcat.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.logcat.data.emitter.ShellLogcatEmitter
import `in`.hridayan.ashell.logcat.data.repository.LogcatFilterRepositoryImpl
import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LogcatModule {

    @Binds
    @Singleton
    abstract fun bindLogcatEmitter(impl: ShellLogcatEmitter): LogcatEmitter

    companion object {
        @Provides
        @Singleton
        fun provideLogcatFilterRepository(
            @ApplicationContext context: Context
        ): LogcatFilterRepository = LogcatFilterRepositoryImpl(context)
    }
}
