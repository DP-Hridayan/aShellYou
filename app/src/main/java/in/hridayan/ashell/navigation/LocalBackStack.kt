package `in`.hridayan.ashell.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("No LocalBackStack provided")
}
