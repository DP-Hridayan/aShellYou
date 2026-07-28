package `in`.hridayan.ashell.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.data.provider.DefaultDispatcherProvider
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }
}
