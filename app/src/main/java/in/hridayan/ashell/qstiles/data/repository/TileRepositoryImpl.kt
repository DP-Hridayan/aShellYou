package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileRepositoryImpl @Inject constructor(
    private val datastore: TileDatastore
) : TileRepository {

    override fun getTiles(): Flow<List<TileConfig>> =
        datastore.getAllTiles().distinctUntilChanged()

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

    override fun getTile(id: Int): Flow<TileConfig?> =
        datastore.getTile(id).distinctUntilChanged()

    override suspend fun getTileOnce(id: Int): TileConfig? =
        datastore.getTileOnce(id)

    override fun getActiveTileCount(): Flow<Int> =
        datastore.getActiveTileCount().distinctUntilChanged()

    override suspend fun getActiveTiles(): List<TileConfig> =
        datastore.getAllTiles().first().filter { it.activeState.isActive }

    /**
     * Toggles the [TileActiveState.isActive] flag for the given tile.
     * For static tiles this is a no-op by design; callers should guard with
     * [TileActiveState.isToggleable] before invoking this.
     */
    override suspend fun toggleTile(id: Int) {
        val tile = datastore.getTileOnce(id) ?: return
        val updated = tile.copy(
            activeState = tile.activeState.copy(isActive = !tile.activeState.isActive)
        )
        datastore.saveTile(updated)
    }

    /**
     * Returns the tile bound to [slotIndex] (0–9).
     * Tile ID is always [slotIndex] + 1.
     */
    override fun getTileBySlot(slotIndex: Int): Flow<TileConfig?> =
        datastore.getTile(slotIndex + 1).distinctUntilChanged()
}