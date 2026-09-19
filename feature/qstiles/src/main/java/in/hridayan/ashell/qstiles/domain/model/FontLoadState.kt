package `in`.hridayan.ashell.qstiles.domain.model

import android.graphics.Typeface
import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry

sealed interface FontLoadState {
    val style: MaterialIconStyle
    
    data class Loading(override val style: MaterialIconStyle) : FontLoadState
    data class Ready(
        override val style: MaterialIconStyle,
        val typeface: Typeface,
        val icons: List<MaterialIconEntry>
    ) : FontLoadState
}
