package `in`.hridayan.ashell.core.common.constants

data class SeedColor(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

sealed class AppSeedColors(val colors: SeedColor) {

    data object LavenderBliss : AppSeedColors(
        SeedColor(
            primary = 0xFF6750A4.toInt(),
            secondary = 0xFF625B71.toInt(),
            tertiary = 0xFF7D5260.toInt()
        )
    )

    data object ForestWhisper : AppSeedColors(
        SeedColor(
            primary = 0xFF386A20.toInt(),
            secondary = 0xFF55624C.toInt(),
            tertiary = 0xFF6B5F40.toInt()
        )
    )

    data object AquaCalm : AppSeedColors(
        SeedColor(
            primary = 0xFF006D77.toInt(),
            secondary = 0xFF83C5BE.toInt(),
            tertiary = 0xFFEDF6F9.toInt()
        )
    )

    data object VioletMist : AppSeedColors(
        SeedColor(
            primary = 0xFF8E4EC6.toInt(),
            secondary = 0xFFB689D2.toInt(),
            tertiary = 0xFFE8D6FA.toInt()
        )
    )

    data object CrimsonDepth : AppSeedColors(
        SeedColor(
            primary = 0xFFB3261E.toInt(),
            secondary = 0xFF775652.toInt(),
            tertiary = 0xFF6F5D7E.toInt()
        )
    )

    data object DeepSea : AppSeedColors(
        SeedColor(
            primary = 0xFF0B7285.toInt(),
            secondary = 0xFF82C0CC.toInt(),
            tertiary = 0xFFD9ED92.toInt()
        )
    )

    data object OceanBreeze : AppSeedColors(
        SeedColor(
            primary = 0xFF005F73.toInt(),
            secondary = 0xFF94D2BD.toInt(),
            tertiary = 0xFFE9D8A6.toInt()
        )
    )

    data object IndigoGlow : AppSeedColors(
        SeedColor(
            primary = 0xFF3B5BDB.toInt(),
            secondary = 0xFF748FFC.toInt(),
            tertiary = 0xFF91A7FF.toInt()
        )
    )

    data object MossHaven : AppSeedColors(
        SeedColor(
            primary = 0xFF5B8C5A.toInt(),
            secondary = 0xFFA4C3A2.toInt(),
            tertiary = 0xFFC2C5AA.toInt()
        )
    )

    data object EarthClay : AppSeedColors(
        SeedColor(
            primary = 0xFF6C584C.toInt(),
            secondary = 0xFFA98467.toInt(),
            tertiary = 0xFFADC178.toInt()
        )
    )

    data object CoralSunset : AppSeedColors(
        SeedColor(
            primary = 0xFFFF6F61.toInt(),
            secondary = 0xFFF4A261.toInt(),
            tertiary = 0xFFE9C46A.toInt()
        )
    )

    data object LeafHarmony : AppSeedColors(
        SeedColor(
            primary = 0xFF52796F.toInt(),
            secondary = 0xFF84A98C.toInt(),
            tertiary = 0xFFCAD2C5.toInt()
        )
    )

    data object AzureBloom : AppSeedColors(
        SeedColor(
            primary = 0xFF1E6091.toInt(),
            secondary = 0xFF76C893.toInt(),
            tertiary = 0xFF99D98C.toInt()
        )
    )

    data object AmberDrift : AppSeedColors(
        SeedColor(
            primary = 0xFFEE9B00.toInt(),
            secondary = 0xFFCA6702.toInt(),
            tertiary = 0xFFBB3E03.toInt()
        )
    )

    data object RoyalOrchid : AppSeedColors(
        SeedColor(
            primary = 0xFF7B2CBF.toInt(),
            secondary = 0xFF9D4EDD.toInt(),
            tertiary = 0xFFC77DFF.toInt()
        )
    )

    data object SkyPulse : AppSeedColors(
        SeedColor(
            primary = 0xFF006494.toInt(),
            secondary = 0xFF247BA0.toInt(),
            tertiary = 0xFF1B98E0.toInt()
        )
    )

    data object TealHarmony : AppSeedColors(
        SeedColor(
            primary = 0xFF264653.toInt(),
            secondary = 0xFF2A9D8F.toInt(),
            tertiary = 0xFFE9C46A.toInt()
        )
    )

    data object RoseClay : AppSeedColors(
        SeedColor(
            primary = 0xFFC44536.toInt(),
            secondary = 0xFFDA5552.toInt(),
            tertiary = 0xFFEDDDD4.toInt()
        )
    )

    data object OliveGrove : AppSeedColors(
        SeedColor(
            primary = 0xFF283618.toInt(),
            secondary = 0xFF606C38.toInt(),
            tertiary = 0xFFFEFAE0.toInt()
        )
    )

    data object BlushMauve : AppSeedColors(
        SeedColor(
            primary = 0xFF6D597A.toInt(),
            secondary = 0xFFB56576.toInt(),
            tertiary = 0xFFE56B6F.toInt()
        )
    )

    companion object {
        val all = listOf(
            LavenderBliss,
            ForestWhisper,
            AquaCalm,
            VioletMist,
            CrimsonDepth,
            DeepSea,
            OceanBreeze,
            IndigoGlow,
            MossHaven,
            EarthClay,
            CoralSunset,
            LeafHarmony,
            AzureBloom,
            AmberDrift,
            RoyalOrchid,
            SkyPulse,
            TealHarmony,
            RoseClay,
            OliveGrove,
            BlushMauve
        )
    }
}
