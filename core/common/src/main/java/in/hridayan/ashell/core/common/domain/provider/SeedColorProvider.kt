package `in`.hridayan.ashell.core.common.domain.provider

import `in`.hridayan.ashell.core.common.data.provider.AppSeedColors
import `in`.hridayan.ashell.core.common.data.provider.SeedColor

object SeedColorProvider {
    val seed = AppSeedColors.Color05.colors

    var primary: Int = seed.seed

    fun setSeedColor(seed: SeedColor) {
        primary = seed.seed
    }
}