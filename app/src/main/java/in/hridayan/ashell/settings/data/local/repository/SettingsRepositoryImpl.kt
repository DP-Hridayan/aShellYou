package `in`.hridayan.ashell.settings.data.local.repository

import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.data.local.SettingsProvider
import `in`.hridayan.ashell.settings.data.local.datastore.SettingsDataStore
import `in`.hridayan.ashell.settings.data.local.model.PreferenceGroup
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

    override suspend fun getLookAndFeelPageList(): List<PreferenceGroup> {
        return SettingsProvider.lookAndFeelPageList
    }

    override suspend fun getDarkThemePageList(): List<PreferenceGroup> {
        return SettingsProvider.darkThemePageList
    }

    override suspend fun getAboutPageList(): List<PreferenceGroup> {
        return SettingsProvider.aboutPageList
    }

    override suspend fun getAutoUpdatePageList(): List<PreferenceGroup> {
        return SettingsProvider.autoUpdatePageList
    }

    override suspend fun getBehaviorPageList(): List<PreferenceGroup> {
        return SettingsProvider.behaviorPageList
    }

    override suspend fun getSettingsPageList(): List<PreferenceGroup> {
        return SettingsProvider.settingsPageList
    }

    override suspend fun getBackupPageList(): List<PreferenceGroup> {
        return SettingsProvider.backupPageList
    }

    override suspend fun getNotificationsPageList(): List<PreferenceGroup> {
        return SettingsProvider.notificationsPageList
    }

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