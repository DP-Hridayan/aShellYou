package `in`.hridayan.ashell.settings.presentation.page.search.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.SearchableSettingsEntry
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.presentation.page.search.SettingsSearchIndex
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

    /** Full search index — built once at init. */
    private val allEntries: List<SearchableSettingsEntry> =
        SettingsSearchIndex.build(context)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Filtered results, grouped by parent screen title. */
    val filteredResults: StateFlow<List<SearchableSettingsEntry>> =
        _query.combine(MutableStateFlow(allEntries)) { q, entries ->
            if (q.isBlank()) emptyList()
            else entries.filter { entry ->
                entry.title.contains(q, ignoreCase = true) ||
                        entry.description.contains(q, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Recent search entries. */
    private val _recentEntries = MutableStateFlow<List<SearchableSettingsEntry>>(emptyList())
    val recentEntries: StateFlow<List<SearchableSettingsEntry>> = _recentEntries.asStateFlow()

    init {
        loadRecentSearches()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    /** Called when the user taps a search result — saves to recent searches. */
    fun onResultClicked(entry: SearchableSettingsEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = loadRecentKeyNames().toMutableList()
            current.remove(entry.settingsKey.name)
            current.add(0, entry.settingsKey.name)
            val trimmed = current.take(MAX_RECENT)
            settingsRepository.setString(
                SettingsKeys.RECENT_SEARCH_KEYS,
                trimmed.joinToString(SEPARATOR)
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

    /** Maps key name strings back to SearchableSettingsEntry objects. */
    private fun resolveEntries(keyNames: List<String>): List<SearchableSettingsEntry> {
        val entryMap = allEntries.associateBy { it.settingsKey.name }
        return keyNames.mapNotNull { entryMap[it] }
    }
}
