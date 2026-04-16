package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider

@Singleton
class TileRepositoryImpl @Inject constructor(
    private val datastore: TileDatastore,
    private val tileComponentManager: TileComponentManager
) : TileRepository {

    /**
     * Mutex guards ALL slot assignment / deactivation logic to prevent race conditions.
     */
    private val slotMutex = Mutex()

    override fun getTiles(): Flow<List<TileConfig>> {
        return datastore.getAllTiles().distinctUntilChanged()
    }

    override suspend fun createTile(config: TileConfig): Int? {
        datastore.saveTile(config)
        return config.id
    }

    override suspend fun updateTile(config: TileConfig) {
        datastore.saveTile(config)
    }

    override suspend fun deleteTile(id: Int) {
        datastore.clearTile(id)
    }

    override fun getTile(id: Int): Flow<TileConfig?> {
        return datastore.getTile(id).distinctUntilChanged()
    }

    override suspend fun getTileOnce(id: Int): TileConfig? {
        return datastore.getTileOnce(id)
    }

    override fun getActiveTileCount(): Flow<Int> {
        return datastore.getActiveTileCount().distinctUntilChanged()
    }

    override suspend fun getActiveTiles(): List<TileConfig> {
        return datastore.getAllTiles().first().filter { it.isActive }
    }

    /**
     * Toggles the active state of a tile.
     */
    override suspend fun toggleTile(id: Int) {
        val tile = datastore.getTileOnce(id) ?: return
        val newState = !tile.isActive
        val updated = tile.copy(isActive = newState)
        datastore.saveTile(updated)
    }


    /**
     * Returns the active tile bound to its fixed [slotIndex] (0–9).
     * Tile ID is always slotIndex + 1.
     */
    override fun getTileBySlot(slotIndex: Int): Flow<TileConfig?> {
        val targetId = slotIndex + 1
        return datastore.getTile(targetId)
            .distinctUntilChanged()
    }
}