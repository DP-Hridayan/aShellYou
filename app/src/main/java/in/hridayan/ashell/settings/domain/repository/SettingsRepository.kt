package `in`.hridayan.ashell.settings.domain.repository

import androidx.datastore.preferences.core.Preferences
import `in`.hridayan.ashell.settings.data.SettingsKeys
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    /** Raw preferences — collect once in a composable to read any key. */
    val preferences: Flow<Preferences>

    fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean>
    suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean)
    suspend fun toggleSetting(key: SettingsKeys<Boolean>)

    fun getInt(key: SettingsKeys<Int>): Flow<Int>
    suspend fun setInt(key: SettingsKeys<Int>, value: Int)

    fun getFloat(key: SettingsKeys<Float>): Flow<Float>
    suspend fun setFloat(key: SettingsKeys<Float>, value: Float)

    fun getString(key: SettingsKeys<String>): Flow<String>
    suspend fun setString(key: SettingsKeys<String>, value: String)

    fun getAllDefaultSettings(): Map<String, Any?>
    suspend fun getCurrentSettings(): Map<String, Any?>
    suspend fun resetAndRestoreDefaults(): Boolean
}