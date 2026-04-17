package `in`.hridayan.ashell.qstiles.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.provider.tileDataStore
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TileDatastore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val ds = context.tileDataStore

    companion object {
        private val TILE_IDS = stringSetPreferencesKey("tile_ids")
    }

    private fun key(id: Int, field: String) = stringPreferencesKey("tile_${id}_$field")
    private fun keyInt(id: Int, field: String) = intPreferencesKey("tile_${id}_$field")
    private fun keyBool(id: Int, field: String) = booleanPreferencesKey("tile_${id}_$field")
    private fun keySlot(id: Int) = intPreferencesKey("tile_${id}_slot")
    private fun keyTimeout(id: Int) = longPreferencesKey("tile_${id}_timeout")

    private fun buildTileConfig(id: Int, prefs: androidx.datastore.preferences.core.Preferences): TileConfig? {
        val name = prefs[key(id, "name")] ?: return null
        return TileConfig(
            id = id,
            name = name,
            command = prefs[key(id, "command")] ?: "",
            executionMode = prefs[keyInt(id, "mode")] ?: 0,
            iconId = prefs[key(id, "icon")] ?: "terminal",
            isActive = prefs[keyBool(id, "active")] ?: false,
            isCustom = prefs[keyBool(id, "custom")] ?: false,
            slotIndex = prefs[keySlot(id)].let { if (it == -1) null else it },
            timeoutMs = prefs[keyTimeout(id)].let { if (it == -1L) null else it }
        )
    }

    fun getTile(tileId: Int): Flow<TileConfig?> {
        return ds.data.map { prefs -> buildTileConfig(tileId, prefs) }
    }

    suspend fun getTileOnce(tileId: Int): TileConfig? {
        return buildTileConfig(tileId, ds.data.first())
    }

    fun getAllTiles(): Flow<List<TileConfig>> {
        return ds.data.map { prefs ->
            val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            ids.mapNotNull { buildTileConfig(it, prefs) }
        }
    }

    suspend fun saveTile(config: TileConfig) {
        ds.edit { prefs ->
            // Register in the ID index
            val existing = prefs[TILE_IDS] ?: emptySet()
            prefs[TILE_IDS] = existing + config.id.toString()

            prefs[key(config.id, "name")] = config.name
            prefs[key(config.id, "command")] = config.command
            prefs[keyInt(config.id, "mode")] = config.executionMode
            prefs[key(config.id, "icon")] = config.iconId
            prefs[keyBool(config.id, "active")] = config.isActive
            prefs[keyBool(config.id, "custom")] = config.isCustom
            // slotIndex: -1 sentinel means null
            prefs[keySlot(config.id)] = config.slotIndex ?: -1
            // timeoutMs: -1 sentinel means null (no timeout)
            prefs[keyTimeout(config.id)] = config.timeoutMs ?: -1L
        }
    }

    suspend fun clearTile(tileId: Int) {
        ds.edit { prefs ->
            // Remove from the ID index
            val existing = prefs[TILE_IDS] ?: emptySet()
            prefs[TILE_IDS] = existing - tileId.toString()

            prefs.remove(key(tileId, "name"))
            prefs.remove(key(tileId, "command"))
            prefs.remove(keyInt(tileId, "mode"))
            prefs.remove(key(tileId, "icon"))
            prefs.remove(keyBool(tileId, "active"))
            prefs.remove(keyBool(tileId, "custom"))
            prefs.remove(keySlot(tileId))
            prefs.remove(keyTimeout(tileId))
        }
    }

    fun getActiveTileCount(): Flow<Int> {
        return ds.data.map { prefs ->
            val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            ids.count { prefs[keyBool(it, "active")] == true }
        }
    }
}