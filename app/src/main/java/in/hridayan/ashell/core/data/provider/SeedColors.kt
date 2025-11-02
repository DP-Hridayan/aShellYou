package `in`.hridayan.ashell.core.data.provider

data class SeedColor(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

sealed class AppSeedColors(val colors: SeedColor) {

    data object Color00 : AppSeedColors(
        SeedColor(
            primary = 0xFFD32F2F.toInt(),
            secondary = 0xFFBCAAA4.toInt(),
            tertiary = 0xFFF57C00.toInt()
        )
    )

    data object Color01 : AppSeedColors(
        SeedColor(
            primary = 0xFFE64A19.toInt(),
            secondary = 0xFFD7CCC8.toInt(),
            tertiary = 0xFFFBC02D.toInt()
        )
    )

    data object Color02 : AppSeedColors(
        SeedColor(
            primary = 0xFFF57C00.toInt(),
            secondary = 0xFFFFE0B2.toInt(),
            tertiary = 0xFFAFB42B.toInt()
        )
    )

    data object Color03 : AppSeedColors(
        SeedColor(
            primary = 0xFFFBC02D.toInt(),
            secondary = 0xFFFFECB3.toInt(),
            tertiary = 0xFF7CB342.toInt()
        )
    )

    data object Color04 : AppSeedColors(
        SeedColor(
            primary = 0xFFAFB42B.toInt(),
            secondary = 0xFFE6EE9C.toInt(),
            tertiary = 0xFF388E3C.toInt()
        )
    )

    data object Color05 : AppSeedColors(
        SeedColor(
            primary = 0xFF7CB342.toInt(),
            secondary = 0xFFC5E1A5.toInt(),
            tertiary = 0xFF00897B.toInt()
        )
    )

    data object Color06 : AppSeedColors(
        SeedColor(
            primary = 0xFF43A047.toInt(),
            secondary = 0xFFA5D6A7.toInt(),
            tertiary = 0xFF0097A7.toInt()
        )
    )

    data object Color07 : AppSeedColors(
        SeedColor(
            primary = 0xFF00897B.toInt(),
            secondary = 0xFFB2DFDB.toInt(),
            tertiary = 0xFF1976D2.toInt()
        )
    )

    data object Color08 : AppSeedColors(
        SeedColor(
            primary = 0xFF0097A7.toInt(),
            secondary = 0xFFB2EBF2.toInt(),
            tertiary = 0xFF5E35B1.toInt()
        )
    )

    data object Color09 : AppSeedColors(
        SeedColor(
            primary = 0xFF1976D2.toInt(),
            secondary = 0xFFBBDEFB.toInt(),
            tertiary = 0xFF8E24AA.toInt()
        )
    )

    data object Color10 : AppSeedColors(
        SeedColor(
            primary = 0xFF1E88E5.toInt(),
            secondary = 0xFF90CAF9.toInt(),
            tertiary = 0xFFAD1457.toInt()
        )
    )

    data object Color11 : AppSeedColors(
        SeedColor(
            primary = 0xFF3949AB.toInt(),
            secondary = 0xFFC5CAE9.toInt(),
            tertiary = 0xFFD32F2F.toInt()
        )
    )

    data object Color12 : AppSeedColors(
        SeedColor(
            primary = 0xFF5E35B1.toInt(),
            secondary = 0xFFD1C4E9.toInt(),
            tertiary = 0xFFF57C00.toInt()
        )
    )

    data object Color13 : AppSeedColors(
        SeedColor(
            primary = 0xFF8E24AA.toInt(),
            secondary = 0xFFE1BEE7.toInt(),
            tertiary = 0xFFFBC02D.toInt()
        )
    )

    data object Color14 : AppSeedColors(
        SeedColor(
            primary = 0xFFAD1457.toInt(),
            secondary = 0xFFF8BBD0.toInt(),
            tertiary = 0xFFAFB42B.toInt()
        )
    )

    data object Color15 : AppSeedColors(
        SeedColor(
            primary = 0xFFD81B60.toInt(),
            secondary = 0xFFF48FB1.toInt(),
            tertiary = 0xFF7CB342.toInt()
        )
    )

    data object Color16 : AppSeedColors(
        SeedColor(
            primary = 0xFFE91E63.toInt(),
            secondary = 0xFFF8BBD0.toInt(),
            tertiary = 0xFF43A047.toInt()
        )
    )

    data object Color17 : AppSeedColors(
        SeedColor(
            primary = 0xFFF06292.toInt(),
            secondary = 0xFFF8BBD0.toInt(),
            tertiary = 0xFF00897B.toInt()
        )
    )

    data object Color18 : AppSeedColors(
        SeedColor(
            primary = 0xFFF48FB1.toInt(),
            secondary = 0xFFFCE4EC.toInt(),
            tertiary = 0xFF1976D2.toInt()
        )
    )

    data object Color19 : AppSeedColors(
        SeedColor(
            primary = 0xFFFFCDD2.toInt(),
            secondary = 0xFFFFEBEE.toInt(),
            tertiary = 0xFF5E35B1.toInt()
        )
    )
}
