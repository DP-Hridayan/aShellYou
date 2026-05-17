package `in`.hridayan.ashell.core.data.local.provider

data class SeedColor(
    val primary: Int,
)

sealed class AppSeedColors(val colors: SeedColor) {

    data object Color01 : AppSeedColors(SeedColor(primary = 0xFFB5353F.toInt()))
    data object Color02 : AppSeedColors(SeedColor(primary = 0xFFF06435.toInt()))
    data object Color03 : AppSeedColors(SeedColor(primary = 0xFFE07200.toInt()))
    data object Color04 : AppSeedColors(SeedColor(primary = 0xFFC78100.toInt()))
    data object Color05 : AppSeedColors(SeedColor(primary = 0xFFB28B00.toInt()))
    data object Color06 : AppSeedColors(SeedColor(primary = 0xFF999419.toInt()))
    data object Color07 : AppSeedColors(SeedColor(primary = 0xFF7D9B36.toInt()))
    data object Color08 : AppSeedColors(SeedColor(primary = 0xFF5BA053.toInt()))
    data object Color09 : AppSeedColors(SeedColor(primary = 0xFF30A370.toInt()))
    data object Color10 : AppSeedColors(SeedColor(primary = 0xFF00A38C.toInt()))
    data object Color11 : AppSeedColors(SeedColor(primary = 0xFF00A1A3.toInt()))
    data object Color12 : AppSeedColors(SeedColor(primary = 0xFF169EB7.toInt()))
    data object Color13 : AppSeedColors(SeedColor(primary = 0xFF389AC7.toInt()))
    data object Color14 : AppSeedColors(SeedColor(primary = 0xFF5695D2.toInt()))
    data object Color15 : AppSeedColors(SeedColor(primary = 0xFF728FD8.toInt()))
    data object Color16 : AppSeedColors(SeedColor(primary = 0xFF8C88D8.toInt()))
    data object Color17 : AppSeedColors(SeedColor(primary = 0xFFA282D1.toInt()))
    data object Color18 : AppSeedColors(SeedColor(primary = 0xFFB67CC2.toInt()))
    data object Color19 : AppSeedColors(SeedColor(primary = 0xFFC677AD.toInt()))
    data object Color20 : AppSeedColors(SeedColor(primary = 0xFFB23268.toInt()))
}
