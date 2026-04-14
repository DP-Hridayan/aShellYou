package `in`.hridayan.ashell.qstiles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import javax.inject.Singleton
import `in`.hridayan.ashell.qstiles.data.repository.*
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository

@Module
@InstallIn(SingletonComponent::class)
object TileModule {

    @Provides
    @Singleton
    fun provideTileRepository(
        datastore: TileDatastore
    ): TileRepository {
        return TileRepositoryImpl(datastore)
    }
}