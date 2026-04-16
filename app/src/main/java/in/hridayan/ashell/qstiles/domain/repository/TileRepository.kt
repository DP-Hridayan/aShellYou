package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow

interface TileRepository {
    /**
     * Creates a new tile by automatically assigning it to the lowest free ID (1..10).
     * @return The assigned ID, or null if all 10 slots are occupied.
     */
    suspend fun createTile(config: TileConfig): Int?

    suspend fun updateTile(config: TileConfig)

    suspend fun deleteTile(id: Int)

    fun getTiles(): Flow<List<TileConfig>>

    fun getTile(id: Int): Flow<TileConfig?>

    suspend fun getTileOnce(id: Int): TileConfig?

    fun getActiveTileCount(): Flow<Int>

    /**
     * Toggles the active state of a tile.
     * Deactivation performs a 'Kick' to move it to the tray.
     */
    suspend fun toggleTile(id: Int)

    /**
     * Returns the active tile mapped to [slotIndex] (0–9), or null if no tile is assigned.
     */
    fun getTileBySlot(slotIndex: Int): Flow<TileConfig?>

    /**
     * Returns a one-shot snapshot of all currently active tiles.
     */
    suspend fun getActiveTiles(): List<TileConfig>
}