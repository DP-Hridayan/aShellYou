package `in`.hridayan.ashell.settings.presentation.page.search.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.presentation.provider.SettingsProvider
import `in`.hridayan.settingsdsl.search.SearchEntry
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

@HiltViewModel
class SettingsSearchViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    companion object {
        private const val MAX_RECENT = 8
        private const val SEPARATOR = ","
    }

    /** Full search index — built once at init via the DSL's SettingsSearchEngine. */
    private val engine: SettingsSearchEngine = SettingsSearchEngine.build(
        context = context,
        pages = SettingsProvider.allSearchablePages,
    )

    private val allEntries: List<SearchEntry> = engine.allEntries()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Filtered results derived from the DSL engine, reactive to [_query]. */
    val filteredResults: StateFlow<List<SearchEntry>> =
        _query.combine(MutableStateFlow(allEntries)) { q, entries ->
            if (q.isBlank()) emptyList()
            else engine.search(q)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _recentEntries = MutableStateFlow<List<SearchEntry>>(emptyList())
    val recentEntries: StateFlow<List<SearchEntry>> = _recentEntries.asStateFlow()

    init {
        loadRecentSearches()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    /** Called when the user taps a search result — persists to recent searches. */
    fun onResultClicked(entry: SearchEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = loadRecentKeyNames().toMutableList()
            current.remove(entry.key.name)
            current.add(0, entry.key.name)
            val trimmed = current.take(MAX_RECENT)
            settingsRepository.setString(
                SettingsKeys.RECENT_SEARCH_KEYS,
                trimmed.joinToString(SEPARATOR),
            )
            _recentEntries.value = resolveEntries(trimmed)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setString(SettingsKeys.RECENT_SEARCH_KEYS, "")
            _recentEntries.value = emptyList()
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            val keyNames = loadRecentKeyNames()
            _recentEntries.value = resolveEntries(keyNames)
        }
    }

    private suspend fun loadRecentKeyNames(): List<String> {
        val raw = settingsRepository.getString(SettingsKeys.RECENT_SEARCH_KEYS)
            .firstOrNull() ?: ""
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    /** Maps key name strings back to [SearchEntry] objects from the engine index. */
    private fun resolveEntries(keyNames: List<String>): List<SearchEntry> {
        val entryMap = allEntries.associateBy { it.key.name }
        return keyNames.mapNotNull { entryMap[it] }
    }
}
