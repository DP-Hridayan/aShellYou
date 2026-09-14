package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface MaterialIconRepository {
    val fontState: StateFlow<FontLoadState>
    suspend fun loadFont(): FontLoadState.Ready
    suspend fun renderAndCacheIcon(iconName: String, codepoint: Int): Result<File>
    fun getCachedIconBitmap(iconName: String): File?
}
