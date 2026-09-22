package `in`.hridayan.ashell.ai.data.remote.catalog

import android.util.Log
import `in`.hridayan.ashell.ai.data.remote.catalog.dto.OpenRouterCatalogResponse
import `in`.hridayan.ashell.core.common.constants.AiModelConstants
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "OpenRouterCatalog"
private const val MODELS_URL = "https://openrouter.ai/api/v1/models"
private const val CACHE_TTL_MILLIS = 24L * 60 * 60 * 1000
private const val CATALOG_TIMEOUT_MILLIS = 8000L

/**
 * Discovers which OpenRouter models are currently free, and which of those accept tool calls.
 *
 * Both facts change often enough that shipping them in the app would guarantee a stale list, so the
 * roster is read from OpenRouter's own catalog. The fetch never blocks a chat turn for long: it has
 * its own short timeout and degrades through the cache to a small seed list.
 */
@Singleton
class OpenRouterModelCatalog @Inject constructor(
    private val httpClient: HttpClient,
    private val cacheStore: OpenRouterModelCacheStore,
) {
    private val mutex = Mutex()

    suspend fun chain(tier: ModelTier, toolsRequired: Boolean): List<String> {
        val catalog = resolveCatalog()
        return OpenRouterModelSelector.chain(
            models = catalog.models,
            tier = tier,
            toolsRequired = toolsRequired,
            contextLengths = catalog.contextLengths,
        )
    }

    private suspend fun resolveCatalog(): OpenRouterModelCacheStore.CachedCatalog = mutex.withLock {
        val cached = cacheStore.read()
        if (cached != null && !cached.isExpired()) return cached

        val fetched = runCatching { fetchCatalog() }
            .onFailure { Log.w(TAG, "Catalog fetch failed, falling back", it) }
            .getOrNull()

        when {
            fetched != null -> fetched.also(cacheStore::write)
            cached != null -> cached
            else -> seedCatalog()
        }
    }

    private suspend fun fetchCatalog(): OpenRouterModelCacheStore.CachedCatalog {
        val response: HttpResponse = httpClient.get(MODELS_URL) {
            timeout { requestTimeoutMillis = CATALOG_TIMEOUT_MILLIS }
        }
        if (response.status != HttpStatusCode.OK) {
            error("Catalog request returned ${response.status.value}")
        }

        val body = response.body<OpenRouterCatalogResponse>()
        val models = OpenRouterModelSelector.toFreeModels(body.data)
        if (models.isEmpty()) error("Catalog contained no free models")

        return OpenRouterModelCacheStore.CachedCatalog(
            fetchedAt = System.currentTimeMillis(),
            models = models,
            contextLengths = OpenRouterModelSelector.contextLengths(body.data),
        )
    }

    private fun seedCatalog(): OpenRouterModelCacheStore.CachedCatalog =
        OpenRouterModelCacheStore.CachedCatalog(
            fetchedAt = 0L,
            models = AiModelConstants.openRouterSeedModels,
        )

    private fun OpenRouterModelCacheStore.CachedCatalog.isExpired(): Boolean =
        System.currentTimeMillis() - fetchedAt > CACHE_TTL_MILLIS
}
