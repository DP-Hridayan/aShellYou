package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TileRepositoryImpl @Inject constructor(
    private val datastore: TileDatastore
) : TileRepository {

    override fun getTile(id: Int): Flow<TileConfig?> =
        datastore.getTile(id)

    override suspend fun saveTile(config: TileConfig) =
        datastore.saveTile(config)

    override suspend fun clearTile(id: Int) =
        datastore.clearTile(id)
}