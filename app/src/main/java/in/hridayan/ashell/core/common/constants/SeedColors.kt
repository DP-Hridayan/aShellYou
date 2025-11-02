package `in`.hridayan.ashell.core.common.constants

data class SeedColor(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

sealed class AppSeedColors(val colors: SeedColor) {

    data object CrimsonDepth : AppSeedColors(
        SeedColor(
            primary = 0xFFC62828.toInt(), // softened red
            secondary = 0xFFFFCDD2.toInt(),
            tertiary = 0xFF00695C.toInt()  // teal-green (90° cw)
        )
    )

    data object RoseClay : AppSeedColors(
        SeedColor(
            primary = 0xFFD75A5A.toInt(),
            secondary = 0xFFFFE0E0.toInt(),
            tertiary = 0xFF1565C0.toInt()  // blue (90° ccw)
        )
    )

    // --- ORANGES ---
    data object CoralSunset : AppSeedColors(
        SeedColor(
            primary = 0xFFEF6C32.toInt(),
            secondary = 0xFFFFD7BA.toInt(),
            tertiary = 0xFF388E3C.toInt()  // greenish
        )
    )

    // --- AMBERS ---
    data object AmberDrift : AppSeedColors(
        SeedColor(
            primary = 0xFFF2A93B.toInt(),
            secondary = 0xFFFFECB3.toInt(),
            tertiary = 0xFF5E35B1.toInt()  // purple side
        )
    )

    // --- GREENS ---
    data object LeafHarmony : AppSeedColors(
        SeedColor(
            primary = 0xFF4CAF50.toInt(),
            secondary = 0xFFC8E6C9.toInt(),
            tertiary = 0xFF9C27B0.toInt()  // magenta opposite of greenish
        )
    )

    data object ForestWhisper : AppSeedColors(
        SeedColor(
            primary = 0xFF388E3C.toInt(),
            secondary = 0xFFA5D6A7.toInt(),
            tertiary = 0xFFFF7043.toInt()  // coral (90° ccw)
        )
    )

    data object MossHaven : AppSeedColors(
        SeedColor(
            primary = 0xFF689F38.toInt(),
            secondary = 0xFFDCEDC8.toInt(),
            tertiary = 0xFF7E57C2.toInt()  // soft violet
        )
    )

    // --- OLIVES ---
    data object OliveGrove : AppSeedColors(
        SeedColor(
            primary = 0xFF827717.toInt(),
            secondary = 0xFFE6EE9C.toInt(),
            tertiary = 0xFF6A1B9A.toInt()
        )
    )

    // --- TEALS ---
    data object TealHarmony : AppSeedColors(
        SeedColor(
            primary = 0xFF00897B.toInt(),
            secondary = 0xFFB2DFDB.toInt(),
            tertiary = 0xFFD32F2F.toInt()
        )
    )

    data object AquaCalm : AppSeedColors(
        SeedColor(
            primary = 0xFF00ACC1.toInt(),
            secondary = 0xFFB2EBF2.toInt(),
            tertiary = 0xFFF57C00.toInt()  // orange-ish
        )
    )

    // --- BLUES ---
    data object DeepSea : AppSeedColors(
        SeedColor(
            primary = 0xFF1565C0.toInt(),
            secondary = 0xFFBBDEFB.toInt(),
            tertiary = 0xFFFF8A65.toInt()  // warm orange
        )
    )

    data object OceanBreeze : AppSeedColors(
        SeedColor(
            primary = 0xFF1E88E5.toInt(),
            secondary = 0xFF90CAF9.toInt(),
            tertiary = 0xFFFFB74D.toInt()
        )
    )

    data object SkyPulse : AppSeedColors(
        SeedColor(
            primary = 0xFF42A5F5.toInt(),
            secondary = 0xFFB3E5FC.toInt(),
            tertiary = 0xFFEC407A.toInt()  // pink accent
        )
    )

    // --- INDIGO / PURPLES ---
    data object IndigoGlow : AppSeedColors(
        SeedColor(
            primary = 0xFF5C6BC0.toInt(),
            secondary = 0xFFC5CAE9.toInt(),
            tertiary = 0xFFFFCA28.toInt()
        )
    )

    data object VioletMist : AppSeedColors(
        SeedColor(
            primary = 0xFF8E24AA.toInt(),
            secondary = 0xFFE1BEE7.toInt(),
            tertiary = 0xFF4DB6AC.toInt()
        )
    )

    data object RoyalOrchid : AppSeedColors(
        SeedColor(
            primary = 0xFF9C27B0.toInt(),
            secondary = 0xFFE1BEE7.toInt(),
            tertiary = 0xFF64B5F6.toInt()  // cyan-blue
        )
    )

    data object LavenderBliss : AppSeedColors(
        SeedColor(
            primary = 0xFF7E57C2.toInt(),
            secondary = 0xFFD1C4E9.toInt(),
            tertiary = 0xFF81C784.toInt()
        )
    )

    // --- PINKS / MAUVES ---
    data object BlushMauve : AppSeedColors(
        SeedColor(
            primary = 0xFFD81B60.toInt(),
            secondary = 0xFFF8BBD0.toInt(),
            tertiary = 0xFF039BE5.toInt()
        )
    )

    // --- EARTHY / NEUTRALS ---
    data object EarthClay : AppSeedColors(
        SeedColor(
            primary = 0xFF8D6E63.toInt(),
            secondary = 0xFFD7CCC8.toInt(),
            tertiary = 0xFF4FC3F7.toInt()
        )
    )

    data object Sandstone : AppSeedColors(
        SeedColor(
            primary = 0xFFBCAAA4.toInt(),
            secondary = 0xFFE0D7D0.toInt(),
            tertiary = 0xFF81C784.toInt()
        )
    )

    data object MistyTaupe : AppSeedColors(
        SeedColor(
            primary = 0xFFA1887F.toInt(),
            secondary = 0xFFD7CCC8.toInt(),
            tertiary = 0xFF64B5F6.toInt()
        )
    )

    data object SlateEcho : AppSeedColors(
        SeedColor(
            primary = 0xFF78909C.toInt(),
            secondary = 0xFFCFD8DC.toInt(),
            tertiary = 0xFFCE93D8.toInt()
        )
    )

    data object StoneWhisper : AppSeedColors(
        SeedColor(
            primary = 0xFF9E9E9E.toInt(),
            secondary = 0xFFE0E0E0.toInt(),
            tertiary = 0xFF81D4FA.toInt()
        )
    )

    data object AshMist : AppSeedColors(
        SeedColor(
            primary = 0xFFB0BEC5.toInt(),
            secondary = 0xFFECEFF1.toInt(),
            tertiary = 0xFFB39DDB.toInt()
        )
    )
}
