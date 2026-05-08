package `in`.hridayan.ashell.settings.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.repository.ContributorsRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.ContributorsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContributorsModule {

    @Provides
    @Singleton
    fun provideContributorsRepository(
        @ApplicationContext context: Context
    ): ContributorsRepository {
        return ContributorsRepositoryImpl(context)
    }
}