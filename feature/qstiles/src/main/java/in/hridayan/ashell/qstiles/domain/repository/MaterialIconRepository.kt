package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.domain.model.MaterialIconStyle
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface MaterialIconRepository {
    val fontState: StateFlow<FontLoadState>
    suspend fun loadFont(style: MaterialIconStyle = MaterialIconStyle.OUTLINED): FontLoadState.Ready
    suspend fun getFontState(style: MaterialIconStyle): FontLoadState.Ready
    suspend fun renderAndCacheIcon(style: MaterialIconStyle, iconName: String, codepoint: Int): Result<File>
    fun getCachedIconBitmap(style: MaterialIconStyle, iconName: String): File?
}
