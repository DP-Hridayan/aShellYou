package `in`.hridayan.composecolorkit.provider

import androidx.compose.ui.graphics.Color
import `in`.hridayan.composecolorkit.seed.SeedColors
import `in`.hridayan.composecolorkit.seed.SeedDefaults


object SeedColorProvider {
    val seed = SeedDefaults.defaultSeed

    var primary: Color = seed.primary
    var secondary: Color = seed.secondary
    var tertiary: Color = seed.tertiary

    fun setSeedColor(seed: SeedColors) {
        primary = seed.primary
        secondary = seed.secondary
        tertiary = seed.tertiary
    }
}