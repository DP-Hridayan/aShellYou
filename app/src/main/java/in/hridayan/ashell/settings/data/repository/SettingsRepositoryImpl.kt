package `in`.hridayan.ashell.settings.data.repository

import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.data.datastore.SettingsDataStore
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun getBoolean(key: SettingsKeys): Flow<Boolean> = dataStore.booleanFlow(key)
    override suspend fun setBoolean(key: SettingsKeys, value: Boolean) =
        dataStore.setBoolean(key, value)

    override suspend fun toggleSetting(key: SettingsKeys) = dataStore.toggle(key)

    override fun getInt(key: SettingsKeys): Flow<Int> = dataStore.intFlow(key)
    override suspend fun setInt(key: SettingsKeys, value: Int) = dataStore.setInt(key, value)

    override fun getFloat(key: SettingsKeys): Flow<Float> = dataStore.floatFlow(key)
    override suspend fun setFloat(key: SettingsKeys, value: Float) = dataStore.setFloat(key, value)

    override fun getString(key: SettingsKeys): Flow<String> = dataStore.stringFlow(key)
    override suspend fun setString(key: SettingsKeys, value: String) =
        dataStore.setString(key, value)

    override fun getAllDefaultSettings(): Map<String, Any?> {
        return dataStore.getAllDefaultSettings()
    }

    override suspend fun getCurrentSettings(): Map<String, Any?> {
        return dataStore.getCurrentSettings()
    }

    override suspend fun resetAndRestoreDefaults(): Boolean {
        return dataStore.resetAndRestoreDefaults()
    }
}