package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.FilterCriteria
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.domain.repository.PackageUidResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The saved profiles, which of them are active, and the search query, combined into the
 * [FilterCriteria] each log list applies. See [criteriaFor] for the per-device part.
 *
 * Profiles and the active set come from storage and survive restarts; the search query and the
 * chosen set live only as long as [scope].
 *
 * A profile can be **active**, filtering the logs, and separately **chosen**, picked by a long press
 * for editing or deleting. Choosing never changes what the logs show. While anything is chosen the
 * list is in selection mode, where a tap chooses or unchooses instead of activating; the mode ends
 * by itself once nothing is chosen.
 *
 * A failed toggle or delete leaves storage unchanged, and the profile list is read back from
 * storage, so the screen never shows a state that was not saved.
 */
class LogFilterSelection(
    private val repository: LogcatFilterRepository,
    private val scope: CoroutineScope,
) {
    val profiles: StateFlow<List<LogFilter>> =
        repository.profiles().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val activeProfileIds: StateFlow<Set<String>> =
        repository.activeProfileIds().stateIn(scope, SharingStarted.Eagerly, emptySet())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val criteria: StateFlow<FilterCriteria> =
        combine(profiles, activeProfileIds, searchQuery) { all, activeIds, query ->
            FilterCriteria(activeProfiles = all.filter { it.id in activeIds }, searchQuery = query)
        }.stateIn(scope, SharingStarted.Eagerly, FilterCriteria())

    /**
     * [criteria] completed with the package UIDs of whichever device [device] currently names, so
     * package filters match that device's logs. Re-resolved when the profiles or the device change;
     * a null device leaves package filters matching nothing.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun criteriaFor(device: Flow<PackageUidResolver?>): StateFlow<FilterCriteria> =
        combine(criteria, device) { base, resolver -> base to resolver }
            .mapLatest { (base, resolver) -> base.withPackageUidsFrom(resolver) }
            .stateIn(scope, SharingStarted.Eagerly, FilterCriteria())

    private val _chosenProfileIds = MutableStateFlow<Set<String>>(emptySet())
    val chosenProfileIds: StateFlow<Set<String>> = _chosenProfileIds.asStateFlow()

    init {
        scope.launch { profiles.collect { all -> dropChosenNotIn(all) } }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun tap(profileId: String) {
        if (_chosenProfileIds.value.isEmpty()) toggleActive(profileId) else toggleChosen(profileId)
    }

    fun longPress(profileId: String) = toggleChosen(profileId)

    fun clearChosen() {
        _chosenProfileIds.value = emptySet()
    }

    fun deleteChosen() {
        val chosen = _chosenProfileIds.value
        clearChosen()
        scope.launch { chosen.forEach { repository.deleteProfile(it) } }
    }

    private fun toggleActive(profileId: String) {
        scope.launch { repository.toggleActiveProfile(profileId) }
    }

    private fun toggleChosen(profileId: String) {
        _chosenProfileIds.update { if (profileId in it) it - profileId else it + profileId }
    }

    private fun dropChosenNotIn(profiles: List<LogFilter>) {
        val existing = profiles.mapTo(mutableSetOf()) { it.id }
        _chosenProfileIds.update { chosen -> chosen intersect existing }
    }

    private suspend fun FilterCriteria.withPackageUidsFrom(resolver: PackageUidResolver?): FilterCriteria {
        val named = packages
        if (resolver == null || named.isEmpty()) return this
        return copy(packageUids = resolver.uidsOf(named))
    }
}
