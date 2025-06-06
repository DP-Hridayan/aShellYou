package `in`.hridayan.ashell.settings.data.local.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.data.local.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds = context.settingsDataStore

    private fun SettingsKeys.toBooleanKey(): Preferences.Key<Boolean> =
        booleanPreferencesKey(this.name)

    fun booleanFlow(key: SettingsKeys): Flow<Boolean> {
        val preferencesKey = key.toBooleanKey()
        val default = key.default as? Boolean == true

        return ds.data.map { prefs ->
            if (!prefs.contains(preferencesKey)) {
                runCatching {
                    context.settingsDataStore.edit { it[preferencesKey] = default }
                }
            }
            prefs[preferencesKey] ?: default
        }
    }

    suspend fun setBoolean(key: SettingsKeys, value: Boolean) {
        val preferencesKey = key.toBooleanKey()
        ds.edit { prefs ->
            prefs[preferencesKey] = value
        }
    }

    suspend fun toggle(key: SettingsKeys) {
        val preferencesKey = key.toBooleanKey()
        ds.edit { prefs ->
            val current = prefs[preferencesKey] == true
            prefs[preferencesKey] = !current
        }
    }

    private fun SettingsKeys.toIntKey(): Preferences.Key<Int> =
        intPreferencesKey(this.name)

    fun intFlow(key: SettingsKeys): Flow<Int> {
        val preferencesKey = key.toIntKey()
        val default = key.default as? Int ?: 0
        return ds.data
            .map { prefs -> prefs[preferencesKey] ?: default }
    }

    suspend fun setInt(key: SettingsKeys, value: Int) {
        val preferencesKey = key.toIntKey()
        ds.edit { prefs ->
            prefs[preferencesKey] = value
        }
    }

    private fun SettingsKeys.toFloatKey(): Preferences.Key<Float> =
        floatPreferencesKey(this.name)

    fun floatFlow(key: SettingsKeys): Flow<Float> {
        val preferencesKey = key.toFloatKey()
        val default = key.default as? Float ?: 0f
        return ds.data
            .map { prefs -> prefs[preferencesKey] ?: default }
    }

    suspend fun setFloat(key: SettingsKeys, value: Float) {
        val preferencesKey = key.toFloatKey()
        ds.edit { prefs ->
            prefs[preferencesKey] = value
        }
    }

    private fun SettingsKeys.toStringKey(): Preferences.Key<String> =
        stringPreferencesKey(this.name)

    fun stringFlow(key: SettingsKeys): Flow<String> {
        val preferencesKey = key.toStringKey()
        val default = key.default as? String ?: ""
        return ds.data
            .map { prefs -> prefs[preferencesKey] ?: default }
    }

    suspend fun setString(key: SettingsKeys, value: String) {
        val preferencesKey = key.toStringKey()
        ds.edit { prefs ->
            prefs[preferencesKey] = value
        }
    }

    fun getAllDefaultSettings(): Map<String, Any?> {
        return SettingsKeys.entries.associate { key -> key.name to key.default }
    }

    suspend fun getCurrentSettings(): Map<String, Any?> {
        val prefs = ds.data.first()

        return SettingsKeys.entries.associate { key ->
            val value = when (val default = key.default) {
                is Boolean -> prefs[booleanPreferencesKey(key.name)] ?: default
                is Int -> prefs[intPreferencesKey(key.name)] ?: default
                is Float -> prefs[floatPreferencesKey(key.name)] ?: default
                else -> null
            }
            key.name to value
        }
    }

    suspend fun resetAndRestoreDefaults(): Boolean {
        val preserveKeys = setOf(
            SettingsKeys.LAST_BACKUP_TIME.name,
            SettingsKeys.SAVED_VERSION_CODE.name,
            SettingsKeys.FIRST_LAUNCH.name
        )

        val currentPrefs = ds.data.first()

        val preservedValues: Map<String, Any?> = preserveKeys.associateWith { keyName ->

            val settingsKey = SettingsKeys.entries.find { it.name == keyName }

            settingsKey?.let { key ->
                when (key.default) {
                    is Boolean -> currentPrefs[booleanPreferencesKey(keyName)]
                    is Int -> currentPrefs[intPreferencesKey(keyName)]
                    is Float -> currentPrefs[floatPreferencesKey(keyName)]
                    is String -> currentPrefs[stringPreferencesKey(keyName)]
                    else -> null
                }
            }
        }

        try {
            ds.edit { prefs ->
                prefs.asMap().keys
                    .filterNot { it.name in preserveKeys }
                    .forEach { prefs.remove(it) }
            }

            ds.edit { prefs ->
                SettingsKeys.entries
                    .filterNot { it.name in preserveKeys }
                    .forEach { key ->
                        when (val defaultValue = key.default) {
                            is Boolean -> prefs[booleanPreferencesKey(key.name)] = defaultValue
                            is Int -> prefs[intPreferencesKey(key.name)] = defaultValue
                            is Float -> prefs[floatPreferencesKey(key.name)] = defaultValue
                            is String -> prefs[stringPreferencesKey(key.name)] = defaultValue
                        }
                    }

                preservedValues.forEach { (keyName, value) ->
                    val settingsKey = SettingsKeys.entries.find { it.name == keyName }
                    if (settingsKey != null && value != null) {
                        when (value) {
                            is Boolean -> prefs[booleanPreferencesKey(keyName)] = value
                            is Int -> prefs[intPreferencesKey(keyName)] = value
                            is Float -> prefs[floatPreferencesKey(keyName)] = value
                            is String -> prefs[stringPreferencesKey(keyName)] = value
                            else -> {}
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            return false
            Log.e("Settings Datastore", e.message ?: "")
        }
    }
}