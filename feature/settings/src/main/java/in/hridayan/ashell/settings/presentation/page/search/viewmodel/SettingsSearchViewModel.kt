package `in`.hridayan.ashell.settings.presentation.page.search.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.settingsdsl.model.SettingsGraph
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

    private val _engine = MutableStateFlow<SettingsSearchEngine?>(null)

    fun setGraphs(graphs: List<SettingsGraph>) {
        if (_engine.value == null) {
            _engine.value = SettingsSearchEngine.build(context, graphs)
            loadRecentSearches()
        }
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val filteredResults: StateFlow<List<SearchEntry>> =
        combine(_query, _engine) { q, engine ->
            if (q.isBlank() || engine == null) {
                emptyList()
            } else {
                engine.search(q)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _recentEntries = MutableStateFlow<List<SearchEntry>>(emptyList())
    val recentEntries: StateFlow<List<SearchEntry>> = _recentEntries.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onResultClicked(entry: SearchEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = loadRecentKeyNames().toMutableList()
            current.remove(entry.key.toString())
            current.add(0, entry.key.toString())
            val trimmed = current.take(MAX_RECENT)
            settingsRepository.setString(
                SettingsKeys.RecentSearchKeys,
                trimmed.joinToString(SEPARATOR),
            )
            _recentEntries.value = resolveEntries(trimmed)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setString(SettingsKeys.RecentSearchKeys, "")
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
        val raw = settingsRepository.getString(SettingsKeys.RecentSearchKeys)
            .firstOrNull() ?: ""
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    private fun resolveEntries(keyNames: List<String>): List<SearchEntry> {
        val engine = _engine.value ?: return emptyList()
        val entryMap = engine.allEntries().associateBy { it.key.toString() }
        return keyNames.mapNotNull { entryMap[it] }
    }
}
