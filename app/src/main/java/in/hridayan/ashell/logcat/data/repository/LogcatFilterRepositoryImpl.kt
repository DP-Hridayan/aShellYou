package `in`.hridayan.ashell.logcat.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

private val Context.logcatFilterDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "logcat_filters")

class LogcatFilterRepositoryImpl @Inject constructor(
    private val context: Context,
) : LogcatFilterRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val FILTERS_KEY = stringPreferencesKey("saved_filters")

    override fun getSavedFilters(): Flow<List<LogFilter>> =
        context.logcatFilterDataStore.data.map { prefs ->
            val raw = prefs[FILTERS_KEY] ?: return@map emptyList()
            runCatching { json.decodeFromString<List<LogFilter>>(raw) }.getOrElse { emptyList() }
        }

    override suspend fun saveFilter(filter: LogFilter) {
        context.logcatFilterDataStore.edit { prefs ->
            val current = prefs[FILTERS_KEY]
                ?.let { runCatching { json.decodeFromString<List<LogFilter>>(it) }.getOrElse { emptyList() } }
                ?: emptyList()
            val updated = current.filterNot { it.id == filter.id } + filter
            prefs[FILTERS_KEY] = json.encodeToString(updated)
        }
    }

    override suspend fun deleteFilter(id: String) {
        context.logcatFilterDataStore.edit { prefs ->
            val current = prefs[FILTERS_KEY]
                ?.let { runCatching { json.decodeFromString<List<LogFilter>>(it) }.getOrElse { emptyList() } }
                ?: emptyList()
            prefs[FILTERS_KEY] = json.encodeToString(current.filterNot { it.id == id })
        }
    }
}
