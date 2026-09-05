package `in`.hridayan.ashell.settings.presentation.page.search.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.settingsdsl.search.SearchGraph
import `in`.hridayan.settingsdsl.search.SearchResult
import `in`.hridayan.settingsdsl.search.SettingsSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_RECENT = 8
private const val SEPARATOR = ","
private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L

@HiltViewModel
class SettingsSearchViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _engine = MutableStateFlow<SettingsSearchEngine?>(null)
    private val _recentIds = MutableStateFlow<List<String>>(emptyList())
    private var recentsLoaded = false

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val filteredResults: StateFlow<List<SearchResult>> =
        combine(_query, _engine) { query, engine ->
            if (query.isBlank() || engine == null) emptyList() else engine.search(query)
        }.stateIn(viewModelScope, subscriptionPolicy(), emptyList())

    val recentEntries: StateFlow<List<SearchResult>> =
        combine(_recentIds, _engine) { ids, engine ->
            engine?.resolve(ids).orEmpty()
        }.stateIn(viewModelScope, subscriptionPolicy(), emptyList())

    /**
     * Rebuilds the index from [graph].
     *
     * Call this whenever the graph instance changes; the caller rebuilds it when an availability
     * gate flips, and a stale index would otherwise keep offering unreachable settings.
     */
    fun setGraph(graph: SearchGraph) {
        _engine.value = SettingsSearchEngine.build(context, graph)
        loadRecentsOnce()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onResultClicked(entry: SearchResult) {
        val updated = (listOf(entry.id) + _recentIds.value.filterNot { it == entry.id })
            .take(MAX_RECENT)
        _recentIds.value = updated
        persistRecents(updated)
    }

    fun clearRecentSearches() {
        _recentIds.value = emptyList()
        persistRecents(emptyList())
    }

    private fun subscriptionPolicy() = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS)

    private fun loadRecentsOnce() {
        if (recentsLoaded) return
        recentsLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            _recentIds.value = readPersistedRecents()
        }
    }

    private fun persistRecents(ids: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setString(
                SettingsKeys.RecentSearchKeys,
                ids.joinToString(SEPARATOR),
            )
        }
    }

    private suspend fun readPersistedRecents(): List<String> {
        val raw = settingsRepository.getString(SettingsKeys.RecentSearchKeys).firstOrNull().orEmpty()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }
}
