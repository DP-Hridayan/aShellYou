package `in`.hridayan.ashell.core.common.data.provider

import androidx.compose.runtime.Immutable

@Immutable
data class SeedColor(
    val seed: Int,
)

@Immutable
sealed class AppSeedColors(val colors: SeedColor) {

    data object Color01 : AppSeedColors(SeedColor(seed = 0xFFB5353F.toInt()))
    data object Color02 : AppSeedColors(SeedColor(seed = 0xFFF06435.toInt()))
    data object Color03 : AppSeedColors(SeedColor(seed = 0xFFE07200.toInt()))
    data object Color04 : AppSeedColors(SeedColor(seed = 0xFFC78100.toInt()))
    data object Color05 : AppSeedColors(SeedColor(seed = 0xFFB28B00.toInt()))
    data object Color06 : AppSeedColors(SeedColor(seed = 0xFF999419.toInt()))
    data object Color07 : AppSeedColors(SeedColor(seed = 0xFF7D9B36.toInt()))
    data object Color08 : AppSeedColors(SeedColor(seed = 0xFF5BA053.toInt()))
    data object Color09 : AppSeedColors(SeedColor(seed = 0xFF30A370.toInt()))
    data object Color10 : AppSeedColors(SeedColor(seed = 0xFF00A38C.toInt()))
    data object Color11 : AppSeedColors(SeedColor(seed = 0xFF00A1A3.toInt()))
    data object Color12 : AppSeedColors(SeedColor(seed = 0xFF169EB7.toInt()))
    data object Color13 : AppSeedColors(SeedColor(seed = 0xFF389AC7.toInt()))
    data object Color14 : AppSeedColors(SeedColor(seed = 0xFF5695D2.toInt()))
    data object Color15 : AppSeedColors(SeedColor(seed = 0xFF728FD8.toInt()))
    data object Color16 : AppSeedColors(SeedColor(seed = 0xFF8C88D8.toInt()))
    data object Color17 : AppSeedColors(SeedColor(seed = 0xFFA282D1.toInt()))
    data object Color18 : AppSeedColors(SeedColor(seed = 0xFFB67CC2.toInt()))
    data object Color19 : AppSeedColors(SeedColor(seed = 0xFFC677AD.toInt()))
    data object Color20 : AppSeedColors(SeedColor(seed = 0xFFB23268.toInt()))
}
