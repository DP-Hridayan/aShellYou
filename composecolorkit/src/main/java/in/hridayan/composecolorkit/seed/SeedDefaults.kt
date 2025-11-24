package `in`.hridayan.composecolorkit.seed

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SeedDefaults {
    val defaultSeed = DefaultSeeds.Color05.colors

    val allDefaultSeeds = listOf(
        listOf(
            DefaultSeeds.Color01,
            DefaultSeeds.Color02,
            DefaultSeeds.Color03,
            DefaultSeeds.Color04,
            DefaultSeeds.Color05,
            DefaultSeeds.Color06,
            DefaultSeeds.Color07,
            DefaultSeeds.Color08,
            DefaultSeeds.Color09,
            DefaultSeeds.Color10,
            DefaultSeeds.Color11,
            DefaultSeeds.Color12,
            DefaultSeeds.Color13,
            DefaultSeeds.Color14,
            DefaultSeeds.Color15,
            DefaultSeeds.Color16,
            DefaultSeeds.Color17,
            DefaultSeeds.Color18,
            DefaultSeeds.Color19,
            DefaultSeeds.Color20
        )
    )

    @Composable
    fun seedColors(
        primary: Color = defaultSeed.primary,
        secondary: Color = defaultSeed.secondary,
        tertiary: Color = defaultSeed.tertiary
    ) = SeedColors(primary, secondary, tertiary)

    sealed class DefaultSeeds(val colors: SeedColors) {

        data object Color01 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFB5353F),
                secondary = Color(0xFFB78483),
                tertiary = Color(0xFFB38A45)
            )
        )

        data object Color02 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFF06435),
                secondary = Color(0xFFB98474),
                tertiary = Color(0xFFA48F42)
            )
        )

        data object Color03 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFE07200),
                secondary = Color(0xFFB2886C),
                tertiary = Color(0xFF929553)
            )
        )

        data object Color04 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFC78100),
                secondary = Color(0xFFA78C6C),
                tertiary = Color(0xFF83976A)
            )
        )

        data object Color05 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFB28B00),
                secondary = Color(0xFF9E8F6D),
                tertiary = Color(0xFF789978)
            )
        )

        data object Color06 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF999419),
                secondary = Color(0xFF959270),
                tertiary = Color(0xFF6E9A86)
            )
        )

        data object Color07 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF7D9B36),
                secondary = Color(0xFF8C9476),
                tertiary = Color(0xFF6A9A92)
            )
        )

        data object Color08 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF5BA053),
                secondary = Color(0xFF84967E),
                tertiary = Color(0xFF69999D)
            )
        )

        data object Color09 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF30A370),
                secondary = Color(0xFF7E9686),
                tertiary = Color(0xFF6D97A6)
            )
        )

        data object Color10 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF00A38C),
                secondary = Color(0xFF7D968F),
                tertiary = Color(0xFF7694AC)
            )
        )

        data object Color11 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF00A1A3),
                secondary = Color(0xFF7D9595),
                tertiary = Color(0xFF8092AE)
            )
        )

        data object Color12 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF169EB7),
                secondary = Color(0xFF7F949B),
                tertiary = Color(0xFF898FB0)
            )
        )

        data object Color13 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF389AC7),
                secondary = Color(0xFF81939F),
                tertiary = Color(0xFF938CAF)
            )
        )

        data object Color14 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF5695D2),
                secondary = Color(0xFF8692A2),
                tertiary = Color(0xFF9D8AAB)
            )
        )

        data object Color15 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF728FD8),
                secondary = Color(0xFF8B90A3),
                tertiary = Color(0xFFA687A4)
            )
        )

        data object Color16 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFF8C88D8),
                secondary = Color(0xFF918EA4),
                tertiary = Color(0xFFAF8599)
            )
        )

        data object Color17 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFA282D1),
                secondary = Color(0xFF978DA2),
                tertiary = Color(0xFFB4848D)
            )
        )

        data object Color18 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFB67CC2),
                secondary = Color(0xFF9D8B9E),
                tertiary = Color(0xFFB7847F)
            )
        )

        data object Color19 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFC677AD),
                secondary = Color(0xFFA38998),
                tertiary = Color(0xFFB78671)
            )
        )

        data object Color20 : DefaultSeeds(
            SeedColors(
                primary = Color(0xFFB23268),
                secondary = Color(0xFFB38491),
                tertiary = Color(0xFFBF844F)
            )
        )
    }
}
