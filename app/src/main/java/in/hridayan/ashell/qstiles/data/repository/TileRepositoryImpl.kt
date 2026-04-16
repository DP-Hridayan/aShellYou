package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
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

@Singleton
class TileRepositoryImpl @Inject constructor(
    private val datastore: TileDatastore
) : TileRepository {

    /**
     * Mutex guards ALL slot assignment / deactivation logic to prevent race conditions.
     */
    private val slotMutex = Mutex()

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
        return datastore.getAllTiles().distinctUntilChanged()
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
     * Returns the active tile bound to [slotIndex] (0–9), or null if no tile
     * is active with that slot. TileService uses this on every tick.
     */
    override fun getTileBySlot(slotIndex: Int): Flow<TileConfig?> {
        return datastore.getAllTiles()
            .map { tiles -> tiles.firstOrNull { it.isActive && it.slotIndex == slotIndex } }
            .distinctUntilChanged()
    }

    /**
     * Finds first available tile ID (not yet used in DataStore).
     * IDs start at 1 and are unbounded for storage.
     */
    override suspend fun getFirstAvailableTileId(): Int? {
        val usedIds = datastore.getAllTiles().first().map { it.id }.toSet()
        return generateSequence(1) { it + 1 }.firstOrNull { it !in usedIds }
    }

    override suspend fun isTileSlotAvailable(id: Int): Boolean {
        return datastore.getTileOnce(id) == null
    }

    /**
     * Activates [tile] by assigning the lowest free slot (0–9).
     * Returns the updated [TileConfig] with [slotIndex] set, or null if all
     * 10 slots are already occupied.
     *
     * The entire read-evaluate-write sequence is enclosed in [slotMutex] to
     * prevent race conditions when multiple tiles are toggled concurrently.
     */
    suspend fun activateTile(tile: TileConfig): TileConfig? = slotMutex.withLock {
        // Tile is already active — no reassignment
        if (tile.isActive && tile.slotIndex != null) return@withLock tile

        val activeTiles = datastore.getAllTiles().first().filter { it.isActive }

        // Enforce 10-tile cap
        if (activeTiles.size >= TileDatastore.MAX_ACTIVE_TILES) return@withLock null

        val usedSlots = activeTiles.mapNotNull { it.slotIndex }.toSet()
        val freeSlot = (0 until TileDatastore.MAX_ACTIVE_TILES).firstOrNull { it !in usedSlots }
            ?: return@withLock null  // Should not happen given the cap above

        val updated = tile.copy(isActive = true, slotIndex = freeSlot)
        datastore.saveTile(updated)
        updated
    }

    /**
     * Deactivates [tile] by clearing its slot assignment.
     * The QS panel tile corresponding to [tile.slotIndex] will then show STATE_UNAVAILABLE.
     */
    suspend fun deactivateTile(tile: TileConfig): Unit = slotMutex.withLock {
        val updated = tile.copy(isActive = false, slotIndex = null)
        datastore.saveTile(updated)
    }
}