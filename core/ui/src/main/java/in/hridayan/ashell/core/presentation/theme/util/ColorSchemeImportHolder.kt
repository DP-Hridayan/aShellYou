package `in`.hridayan.ashell.core.presentation.theme.util

import `in`.hridayan.ashell.core.presentation.theme.data.ColorSchemePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorSchemeImportHolder @Inject constructor() {
    private val _importedScheme = MutableStateFlow<ColorSchemePayload?>(null)
    val importedTheme = _importedScheme.asStateFlow()

    fun setImportedColorScheme(theme: ColorSchemePayload) {
        _importedScheme.value = theme
    }

    fun clearImportedColorScheme() {
        _importedScheme.value = null
    }
}
