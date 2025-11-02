package `in`.hridayan.ashell.core.common

import `in`.hridayan.ashell.core.common.constants.AppSeedColors
import `in`.hridayan.ashell.core.common.constants.SeedColor

object SeedColorProvider {
    val seed = AppSeedColors.Color00.colors

    var primary: Int = seed.primary
    var secondary: Int = seed.secondary
    var tertiary: Int = seed.tertiary

    fun setSeedColor(seed: SeedColor) {
        primary = seed.primary
        secondary = seed.secondary
        tertiary = seed.tertiary
    }
}
