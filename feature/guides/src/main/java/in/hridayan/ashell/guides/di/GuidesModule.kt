package `in`.hridayan.ashell.guides.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.guides.data.repository.GuidesRepositoryImpl
import `in`.hridayan.ashell.guides.domain.repository.GuidesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuidesModule {

    @Binds
    @Singleton
    abstract fun bindGuidesRepository(
        guidesRepositoryImpl: GuidesRepositoryImpl
    ): GuidesRepository
}
