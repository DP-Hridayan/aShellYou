package `in`.hridayan.ashell.qstiles.domain.model

import android.graphics.Typeface
import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry

sealed interface FontLoadState {
    data object NotDownloaded : FontLoadState
    data object Loading : FontLoadState
    data class Ready(val typeface: Typeface, val icons: List<MaterialIconEntry>) : FontLoadState
    data class Error(val message: String) : FontLoadState
}
