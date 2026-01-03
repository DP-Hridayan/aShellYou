package `in`.hridayan.ashell.core.domain.provider

import `in`.hridayan.ashell.core.data.local.provider.AppSeedColors
import `in`.hridayan.ashell.core.data.local.provider.SeedColor

object SeedColorProvider {
    val seed = AppSeedColors.Color05.colors

    var primary: Int = seed.primary
    var secondary: Int = seed.secondary
    var tertiary: Int = seed.tertiary

    fun setSeedColor(seed: SeedColor) {
        primary = seed.primary
        secondary = seed.secondary
        tertiary = seed.tertiary
    }
}