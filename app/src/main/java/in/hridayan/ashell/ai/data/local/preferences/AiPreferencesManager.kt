package `in`.hridayan.ashell.ai.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_preferences")

/**
 * Manages AI-related preferences using DataStore.
 */
@Singleton
class AiPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val CACHE_ENABLED = booleanPreferencesKey("ai_cache_enabled")
        val MAX_CACHE_AGE_DAYS = intPreferencesKey("ai_max_cache_age_days")
    }

    /** Currently selected model ID */
    val selectedModelId: Flow<String> = context.aiDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_MODEL_ID] ?: ModelRegistry.defaultModelId
    }

    /** Whether AI analysis caching is enabled */
    val cacheEnabled: Flow<Boolean> = context.aiDataStore.data.map { prefs ->
        prefs[Keys.CACHE_ENABLED] ?: true
    }

    /** Maximum cache age in days */
    val maxCacheAgeDays: Flow<Int> = context.aiDataStore.data.map { prefs ->
        prefs[Keys.MAX_CACHE_AGE_DAYS] ?: 30
    }

    suspend fun setSelectedModelId(modelId: String) {
        context.aiDataStore.edit { prefs ->
            prefs[Keys.SELECTED_MODEL_ID] = modelId
        }
    }

    suspend fun setCacheEnabled(enabled: Boolean) {
        context.aiDataStore.edit { prefs ->
            prefs[Keys.CACHE_ENABLED] = enabled
        }
    }

    suspend fun setMaxCacheAgeDays(days: Int) {
        context.aiDataStore.edit { prefs ->
            prefs[Keys.MAX_CACHE_AGE_DAYS] = days
        }
    }
}
