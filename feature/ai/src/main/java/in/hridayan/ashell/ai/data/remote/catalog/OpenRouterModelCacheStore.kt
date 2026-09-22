package `in`.hridayan.ashell.ai.data.remote.catalog

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.LlmModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "openrouter_model_cache"
private const val KEY_SNAPSHOT = "snapshot"
private const val TAG = "OpenRouterModelCache"

/**
 * Persists the last known OpenRouter free roster.
 *
 * This deliberately lives in its own preferences file rather than in app settings: settings feed
 * backup, restore and "reset to defaults", and a cached model roster has no business travelling
 * between devices or surviving a reset that was meant to clear user choices.
 */
@Singleton
class OpenRouterModelCacheStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun read(): CachedCatalog? {
        val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return null
        return try {
            json.decodeFromString<CachedCatalog>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Unreadable cached roster; it will be fetched again", e)
            null
        }
    }

    fun write(catalog: CachedCatalog) {
        prefs.edit { putString(KEY_SNAPSHOT, json.encodeToString(catalog)) }
    }

    @Serializable
    data class CachedCatalog(
        val fetchedAt: Long,
        val models: List<LlmModel>,
        val contextLengths: Map<String, Long> = emptyMap(),
    )
}
