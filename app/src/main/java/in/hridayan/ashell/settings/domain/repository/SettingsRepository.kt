package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.data.SettingsKeys
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getBoolean(key: SettingsKeys): Flow<Boolean>
    suspend fun setBoolean(key: SettingsKeys, value: Boolean)
    suspend fun toggleSetting(key: SettingsKeys)

    fun getInt(key: SettingsKeys): Flow<Int>
    suspend fun setInt(key: SettingsKeys, value: Int)

    fun getFloat(key: SettingsKeys): Flow<Float>
    suspend fun setFloat(key: SettingsKeys, value: Float)

    fun getString(key: SettingsKeys): Flow<String>
    suspend fun setString(key: SettingsKeys, value: String)

    fun getAllDefaultSettings(): Map<String, Any?>
    suspend fun getCurrentSettings(): Map<String, Any?>
    suspend fun resetAndRestoreDefaults(): Boolean
}