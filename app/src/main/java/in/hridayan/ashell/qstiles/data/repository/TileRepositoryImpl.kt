package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TileRepositoryImpl @Inject constructor(
    private val datastore: TileDatastore
) : TileRepository {
    override suspend fun createTile(config: TileConfig) {
        datastore.saveTile(config)
    }

    override suspend fun updateTile(config: TileConfig) {
        datastore.saveTile(config)
    }

    override suspend fun deleteTile(id: Int) {
        datastore.clearTile(id)
    }

    override fun getTiles(): Flow<List<TileConfig>> {
        return datastore.getAllTiles()
    }

    override fun getTile(id: Int): Flow<TileConfig?> {
        return datastore.getTile(id)
    }

    override suspend fun getTileOnce(id: Int): TileConfig? {
        return datastore.getTileOnce(id)
    }

    override fun getActiveTileCount(): Flow<Int> {
        return datastore.getActiveTileCount()
    }

    /**
     * Finds first free slot (1–10)
     */
    override suspend fun getFirstAvailableTileId(): Int? {
        val tiles = datastore.getAllTiles().first()
        val usedIds = tiles.map { it.id }

        return (1..TileDatastore.MAX_TILES)
            .firstOrNull { it !in usedIds }
    }

    /**
     * Check if slot is free
     */
    override suspend fun isTileSlotAvailable(id: Int): Boolean {
        val tile = datastore.getTileOnce(id)
        return tile == null
    }
}