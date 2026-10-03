package `in`.hridayan.ashell.logcat.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.InstalledApp
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.model.matches
import `in`.hridayan.ashell.logcat.domain.repository.InstalledAppsRepository
import `in`.hridayan.ashell.logcat.domain.repository.LogcatFilterRepository
import `in`.hridayan.ashell.logcat.presentation.event.FilterEditorEvent
import `in`.hridayan.ashell.logcat.presentation.model.FilterProfileForm
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Backs the screen that creates or edits one filter profile, including the picker that fills its
 * packages from this device's installed apps. A new profile becomes active when it is saved; an
 * edited one keeps its current active state.
 */
@HiltViewModel
class LogcatFilterEditorViewModel internal constructor(
    private val profileId: String?,
    private val repository: LogcatFilterRepository,
    private val installedApps: InstalledAppsRepository,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: LogcatFilterRepository,
        installedApps: InstalledAppsRepository,
    ) : this(
        profileId = savedStateHandle.toRoute<NavRoutes.LogcatFilterEditorScreen>().profileId,
        repository = repository,
        installedApps = installedApps,
    )

    val isEditing: Boolean = profileId != null

    private val _form = MutableStateFlow(FilterProfileForm())
    val form: StateFlow<FilterProfileForm> = _form.asStateFlow()

    private val _events = MutableSharedFlow<FilterEditorEvent>()
    val events: SharedFlow<FilterEditorEvent> = _events.asSharedFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>?>(null)
    private var appsRequested = false

    private val _appQuery = MutableStateFlow("")
    val appQuery: StateFlow<String> = _appQuery.asStateFlow()

    /** Installed apps matching [appQuery], or null while the list is still loading. */
    val visibleApps: StateFlow<List<InstalledApp>?> =
        combine(_installedApps, _appQuery) { apps, query -> apps?.filter { it.matches(query) } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        profileId?.let { loadProfile(it) }
    }

    fun loadInstalledApps() {
        if (appsRequested) return
        appsRequested = true
        viewModelScope.launch {
            _installedApps.value = installedApps.installedApps().getOrElse { emptyList() }
        }
    }

    fun onAppQueryChange(query: String) {
        _appQuery.value = query
    }

    fun onPackagesChange(text: String) = _form.update { it.copy(packages = text) }

    fun onPackageToggle(packageName: String) = _form.update { it.withPackageToggled(packageName) }

    fun onNameChange(name: String) = _form.update { it.copy(name = name) }

    fun onModeChange(mode: FilterMode) = _form.update { it.withMode(mode) }

    fun onLevelToggle(level: LogLevel) = _form.update { it.withLevelToggled(level) }

    fun onTagsChange(text: String) = _form.update { it.copy(tags = text) }

    fun onPidsChange(text: String) = _form.update { it.copy(pids = text) }

    fun onTidsChange(text: String) = _form.update { it.copy(tids = text) }

    fun save() {
        val current = _form.value
        if (!current.isValid) {
            _form.update { it.copy(showNameError = true) }
            return
        }
        viewModelScope.launch {
            val profile = current.toProfile(profileId ?: UUID.randomUUID().toString())
            val result = repository.saveProfile(profile, activate = !isEditing)
            _events.emit(if (result.isSuccess) FilterEditorEvent.Saved else FilterEditorEvent.SaveFailed)
        }
    }

    private fun loadProfile(id: String) {
        viewModelScope.launch {
            val profile = repository.profiles().first().firstOrNull { it.id == id } ?: return@launch
            _form.value = FilterProfileForm.from(profile)
        }
    }
}
