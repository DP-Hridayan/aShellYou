package `in`.hridayan.ashell.qstiles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import javax.inject.Singleton
import `in`.hridayan.ashell.qstiles.data.repository.*
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.processor.TileIconMatcher
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.domain.usecase.CreateTileUseCase
import `in`.hridayan.ashell.qstiles.domain.usecase.ExecuteTileUseCase

@Module
@InstallIn(SingletonComponent::class)
object TileModule {

    @Provides
    fun provideTileRepository(impl: TileRepositoryImpl): TileRepository = impl

    @Provides
    fun provideLogRepository(impl: TileLogRepositoryImpl): TileLogRepository = impl

    @Provides
    fun provideIconMatcher(): TileIconMatcher {
        return TileIconMatcher(TileCommandKeywordProcessor())
    }

    @Provides
    fun provideCreateTileUseCase(
        repo: TileRepository,
        matcher: TileIconMatcher
    ) = CreateTileUseCase(repo, matcher)

    @Provides
    fun provideExecuteTileUseCase(
        logRepo: TileLogRepository
    ) = ExecuteTileUseCase(logRepo)
}