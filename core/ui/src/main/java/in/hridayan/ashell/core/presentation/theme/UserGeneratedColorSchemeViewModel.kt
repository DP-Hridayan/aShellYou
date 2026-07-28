package `in`.hridayan.ashell.core.presentation.theme

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class UserGeneratedColorSchemeViewModel @Inject constructor(
    private val customColorSchemeDao: CustomColorSchemeDao
) : ViewModel() {

    private val _activeUserGeneratedColorScheme = MutableStateFlow<UserGeneratedColorScheme?>(null)
    val activeUserGeneratedColorScheme = _activeUserGeneratedColorScheme.asStateFlow()

    fun loadGeneratedColorScheme(id: Int) {
        if (id == 0) {
            _activeUserGeneratedColorScheme.value = null
            return
        }
        viewModelScope.launch {
            val entity = customColorSchemeDao.getColorSchemeById(id)
            _activeUserGeneratedColorScheme.value = entity?.toDomain()
        }
    }
}
