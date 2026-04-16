package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow

interface TileRepository {
    suspend fun createTile(config: TileConfig)

    suspend fun updateTile(config: TileConfig)

    suspend fun deleteTile(id: Int)

    fun getTiles(): Flow<List<TileConfig>>

    fun getTile(id: Int): Flow<TileConfig?>

    suspend fun getTileOnce(id: Int): TileConfig?

    fun getActiveTileCount(): Flow<Int>

    suspend fun getFirstAvailableTileId(): Int?

    suspend fun isTileSlotAvailable(id: Int): Boolean

    /**
     * Returns the active tile mapped to [slotIndex] (0–9), or null if no tile is assigned.
     */
    fun getTileBySlot(slotIndex: Int): Flow<TileConfig?>

    /**
     * Returns a one-shot snapshot of all currently active tiles.
     */
    suspend fun getActiveTiles(): List<TileConfig>
}