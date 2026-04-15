package `in`.hridayan.ashell.qstiles.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        const val MAX_TILES = 10
    }

    private fun key(id: Int, field: String) =
        stringPreferencesKey("tile_${id}_$field")

    private fun keyInt(id: Int, field: String) =
        intPreferencesKey("tile_${id}_$field")

    private fun keyBool(id: Int, field: String) =
        booleanPreferencesKey("tile_${id}_$field")

    fun getTile(tileId: Int): Flow<TileConfig?> {
        return ds.data.map { prefs ->
            val name = prefs[key(tileId, "name")] ?: return@map null

            TileConfig(
                id = tileId,
                name = name,
                command = prefs[key(tileId, "command")] ?: "",
                executionMode = prefs[keyInt(tileId, "mode")] ?: 0,
                iconId = prefs[key(tileId, "icon")] ?: "terminal",
                isActive = prefs[keyBool(tileId, "active")] ?: false,
                isCustom = prefs[keyBool(tileId, "custom")] ?: false
            )
        }
    }

    suspend fun getTileOnce(tileId: Int): TileConfig? {
        val prefs = ds.data.first()

        val name = prefs[key(tileId, "name")] ?: return null

        return TileConfig(
            id = tileId,
            name = name,
            command = prefs[key(tileId, "command")] ?: "",
            executionMode = prefs[keyInt(tileId, "mode")] ?: 0,
            iconId = prefs[key(tileId, "icon")] ?: "terminal",
            isActive = prefs[keyBool(tileId, "active")] ?: false,
            isCustom = prefs[keyBool(tileId, "custom")] ?: false
        )
    }

    fun getAllTiles(): Flow<List<TileConfig>> {
        return ds.data.map { prefs ->
            (1..MAX_TILES).mapNotNull { id ->
                val name = prefs[key(id, "name")] ?: return@mapNotNull null

                TileConfig(
                    id = id,
                    name = name,
                    command = prefs[key(id, "command")] ?: "",
                    executionMode = prefs[keyInt(id, "mode")] ?: 0,
                    iconId = prefs[key(id, "icon")] ?: "terminal",
                    isActive = prefs[keyBool(id, "active")] ?: false,
                    isCustom = prefs[keyBool(id, "custom")] ?: false
                )
            }
        }
    }

    suspend fun saveTile(config: TileConfig) {
        ds.edit { prefs ->
            prefs[key(config.id, "name")] = config.name
            prefs[key(config.id, "command")] = config.command
            prefs[keyInt(config.id, "mode")] = config.executionMode
            prefs[key(config.id, "icon")] = config.iconId
            prefs[keyBool(config.id, "active")] = config.isActive
            prefs[keyBool(config.id, "custom")] = config.isCustom
        }
    }

    suspend fun clearTile(tileId: Int) {
        ds.edit { prefs ->
            prefs.remove(key(tileId, "name"))
            prefs.remove(key(tileId, "command"))
            prefs.remove(keyInt(tileId, "mode"))
            prefs.remove(key(tileId, "icon"))
            prefs.remove(keyBool(tileId, "active"))
            prefs.remove(keyBool(tileId, "custom"))
        }
    }

    fun getActiveTileCount(): Flow<Int> {
        return ds.data.map { prefs ->
            (1..MAX_TILES).count { id ->
                prefs[keyBool(id, "active")] == true
            }
        }
    }
}