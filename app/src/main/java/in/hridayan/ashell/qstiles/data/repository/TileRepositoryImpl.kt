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

    override suspend fun createTile(config: TileConfig): Int? = slotMutex.withLock {
        // Find lowest free ID in range 1..10
        val nextId = (1..10).firstOrNull { datastore.getTileOnce(it) == null } ?: return@withLock null

        val tileWithId = config.copy(
            id = nextId,
            slotIndex = nextId - 1,
            isActive = true // Map and Enable immediately
        )
        datastore.saveTile(tileWithId)

        // Ensure component is enabled so it shows up in panel
        tileComponentManager.setComponentEnabled(tileWithId.slotIndex!!, true)
        
        // PROMPT: (Android 13+) Ask user to add to active panel
        tileComponentManager.promptAddTile(
            tileWithId.slotIndex!!,
            tileWithId.name,
            TileIconProvider.getIconRes(tileWithId.iconId)
        )

        nextId
    }

    override suspend fun updateTile(config: TileConfig) {
        datastore.saveTile(config)
    }

    override suspend fun deleteTile(id: Int) = slotMutex.withLock {
        val tile = datastore.getTileOnce(id)
        val slot = tile?.slotIndex
        datastore.clearTile(id)
        
        // Reset slot identity by kicking it to the tray (PackageManager toggle)
        if (slot != null) {
            tileComponentManager.kickComponent(slot)
        }
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
     * Toggles the active state of a tile.
     * Maps to panel highlight and tray visibility.
     */
    override suspend fun toggleTile(id: Int) = slotMutex.withLock {
        val tile = datastore.getTileOnce(id) ?: return@withLock
        val newState = !tile.isActive
        val updated = tile.copy(isActive = newState)
        datastore.saveTile(updated)

        // Slot is always id - 1
        val slot = id - 1
        if (!newState) {
            // Kick to tray
            tileComponentManager.kickComponent(slot)
        } else {
            // Enable in panel (if system remembers) or just stay enabled
            tileComponentManager.setComponentEnabled(slot, true)
            
            // PROMPT: (Android 13+) Helper to move it back to active panel
            tileComponentManager.promptAddTile(
                slot,
                tile.name,
                TileIconProvider.getIconRes(tile.iconId)
            )
        }
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