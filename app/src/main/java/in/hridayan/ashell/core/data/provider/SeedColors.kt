package `in`.hridayan.ashell.core.data.provider

data class SeedColor(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

sealed class AppSeedColors(val colors: SeedColor) {

    data object Color01 : AppSeedColors(
        SeedColor(
            primary = 0xFFB5353F.toInt(),
            secondary = 0xFFB78483.toInt(),
            tertiary = 0xFFB38A45.toInt()
        )
    )

    data object Color02 : AppSeedColors(
        SeedColor(
            primary = 0xFFF06435.toInt(),
            secondary = 0xFFB98474.toInt(),
            tertiary = 0xFFA48F42.toInt()
        )
    )

    data object Color03 : AppSeedColors(
        SeedColor(
            primary = 0xFFE07200.toInt(),
            secondary = 0xFFB2886C.toInt(),
            tertiary = 0xFF929553.toInt()
        )
    )

    data object Color04 : AppSeedColors(
        SeedColor(
            primary = 0xFFC78100.toInt(),
            secondary = 0xFFA78C6C.toInt(),
            tertiary = 0xFF83976A.toInt()
        )
    )

    data object Color05 : AppSeedColors(
        SeedColor(
            primary = 0xFFB28B00.toInt(),
            secondary = 0xFF9E8F6D.toInt(),
            tertiary = 0xFF789978.toInt()
        )
    )

    data object Color06 : AppSeedColors(
        SeedColor(
            primary = 0xFF999419.toInt(),
            secondary = 0xFF959270.toInt(),
            tertiary = 0xFF6E9A86.toInt()
        )
    )

    data object Color07 : AppSeedColors(
        SeedColor(
            primary = 0xFF7D9B36.toInt(),
            secondary = 0xFF8C9476.toInt(),
            tertiary = 0xFF6A9A92.toInt()
        )
    )

    data object Color08 : AppSeedColors(
        SeedColor(
            primary = 0xFF5BA053.toInt(),
            secondary = 0xFF84967E.toInt(),
            tertiary = 0xFF69999D.toInt()
        )
    )

    data object Color09 : AppSeedColors(
        SeedColor(
            primary = 0xFF30A370.toInt(),
            secondary = 0xFF7E9686.toInt(),
            tertiary = 0xFF6D97A6.toInt()
        )
    )

    data object Color10 : AppSeedColors(
        SeedColor(
            primary = 0xFF00A38C.toInt(),
            secondary = 0xFF7D968F.toInt(),
            tertiary = 0xFF7694AC.toInt()
        )
    )

    data object Color11 : AppSeedColors(
        SeedColor(
            primary = 0xFF00A1A3.toInt(),
            secondary = 0xFF7D9595.toInt(),
            tertiary = 0xFF8092AE.toInt()
        )
    )

    data object Color12 : AppSeedColors(
        SeedColor(
            primary = 0xFF169EB7.toInt(),
            secondary = 0xFF7F949B.toInt(),
            tertiary = 0xFF898FB0.toInt()
        )
    )

    data object Color13 : AppSeedColors(
        SeedColor(
            primary = 0xFF389AC7.toInt(),
            secondary = 0xFF81939F.toInt(),
            tertiary = 0xFF938CAF.toInt()
        )
    )

    data object Color14 : AppSeedColors(
        SeedColor(
            primary = 0xFF5695D2.toInt(),
            secondary = 0xFF8692A2.toInt(),
            tertiary = 0xFF9D8AAB.toInt()
        )
    )

    data object Color15 : AppSeedColors(
        SeedColor(
            primary = 0xFF728FD8.toInt(),
            secondary = 0xFF8B90A3.toInt(),
            tertiary = 0xFFA687A4.toInt()
        )
    )

    data object Color16 : AppSeedColors(
        SeedColor(
            primary = 0xFF8C88D8.toInt(),
            secondary = 0xFF918EA4.toInt(),
            tertiary = 0xFFAF8599.toInt()
        )
    )

    data object Color17 : AppSeedColors(
        SeedColor(
            primary = 0xFFA282D1.toInt(),
            secondary = 0xFF978DA2.toInt(),
            tertiary = 0xFFB4848D.toInt()
        )
    )

    data object Color18 : AppSeedColors(
        SeedColor(
            primary = 0xFFB67CC2.toInt(),
            secondary = 0xFF9D8B9E.toInt(),
            tertiary = 0xFFB7847F.toInt()
        )
    )

    data object Color19 : AppSeedColors(
        SeedColor(
            primary = 0xFFC677AD.toInt(),
            secondary = 0xFFA38998.toInt(),
            tertiary = 0xFFB78671.toInt()
        )
    )

    data object Color20 : AppSeedColors(
        SeedColor(
            primary = 0xFFB23268.toInt(),
            secondary = 0xFFB38491.toInt(),
            tertiary = 0xFFBF844F.toInt()
        )
    )
}
