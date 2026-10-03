package `in`.hridayan.ashell.logcat.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

private val Context.logcatFilterDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "logcat_filters")

private val profilesKey = stringPreferencesKey("saved_filters")
private val activeProfileIdsKey = stringSetPreferencesKey("active_profile_ids")

class LogcatFilterRepositoryImpl @Inject constructor(
    private val context: Context,
) : LogcatFilterRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun profiles(): Flow<List<LogFilter>> = preferences().map { decodeProfiles(it) }

    override fun activeProfileIds(): Flow<Set<String>> =
        preferences().map { it[activeProfileIdsKey].orEmpty() }

    override suspend fun saveProfile(profile: LogFilter, activate: Boolean): Result<Unit> =
        write { prefs ->
            prefs[profilesKey] = encodeProfiles(decodeProfiles(prefs).upsert(profile))
            if (activate) prefs[activeProfileIdsKey] = prefs[activeProfileIdsKey].orEmpty() + profile.id
        }

    override suspend fun deleteProfile(id: String): Result<Unit> = write { prefs ->
        prefs[profilesKey] = encodeProfiles(decodeProfiles(prefs).filterNot { it.id == id })
        prefs[activeProfileIdsKey] = prefs[activeProfileIdsKey].orEmpty() - id
    }

    override suspend fun toggleActiveProfile(id: String): Result<Unit> = write { prefs ->
        prefs[activeProfileIdsKey] = prefs[activeProfileIdsKey].orEmpty().toggled(id)
    }

    private fun preferences(): Flow<Preferences> = context.logcatFilterDataStore.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }

    private suspend fun write(transform: (MutablePreferences) -> Unit): Result<Unit> = try {
        context.logcatFilterDataStore.edit { transform(it) }
        Result.success(Unit)
    } catch (e: IOException) {
        Result.failure(e)
    }

    private fun decodeProfiles(prefs: Preferences): List<LogFilter> {
        val raw = prefs[profilesKey] ?: return emptyList()
        return runCatching { json.decodeFromString<List<LogFilter>>(raw) }.getOrElse { emptyList() }
    }

    private fun encodeProfiles(profiles: List<LogFilter>): String = json.encodeToString(profiles)
}
