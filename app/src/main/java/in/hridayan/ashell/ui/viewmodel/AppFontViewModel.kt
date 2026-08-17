package `in`.hridayan.ashell.ui.viewmodel

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.AppFont
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

/**
 * Resolves the active custom [FontFamily] for the app-wide theme.
 *
 * Uses [Typeface.createFromFile] on [Dispatchers.IO] so the typeface is
 * fully loaded before it reaches the composition. [Font] wrapping a
 * pre-loaded [Typeface] has [FontLoadingStrategy.Blocking] that returns
 * instantly — no disk I/O on the main thread at layout time.
 *
 * [SharingStarted.Eagerly] ensures the font is ready before the first
 * screen opens, eliminating first-open lag on every navigation destination.
 */
@HiltViewModel
class AppFontViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val customFontRepository: CustomFontRepository
) : ViewModel() {

    val activeCustomFontFamily: StateFlow<FontFamily?> = combine(
        settingsRepository.getInt(SettingsKeys.FontFamily),
        customFontRepository.getAllCustomFonts()
    ) { selectedId, fonts ->
        if (selectedId >= AppFont.CUSTOM_FONT_ID_OFFSET) {
            fonts.find { it.id == selectedId }
                ?.let { entity ->
                    runCatching {
                        val typeface = Typeface.createFromFile(File(entity.filePath))
                        FontFamily(typeface)
                    }.getOrNull()
                }
        } else null
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
