package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.data.local.model.PreferenceGroup
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

    suspend fun getSettingsPageList(): List<PreferenceGroup>
    suspend fun getLookAndFeelPageList(): List<PreferenceGroup>
    suspend fun getAboutPageList(): List<PreferenceGroup>
    suspend fun getAutoUpdatePageList(): List<PreferenceGroup>
    suspend fun getDarkThemePageList(): List<PreferenceGroup>
    suspend fun getBehaviorPageList(): List<PreferenceGroup>
    suspend fun getBackupPageList(): List<PreferenceGroup>

    fun getAllDefaultSettings(): Map<String, Any?>
    suspend fun getCurrentSettings(): Map<String, Any?>
    suspend fun resetAndRestoreDefaults(): Boolean
}