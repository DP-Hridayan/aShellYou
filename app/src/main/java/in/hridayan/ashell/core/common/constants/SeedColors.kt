package `in`.hridayan.ashell.core.common.constants

import androidx.compose.ui.graphics.Color

sealed class SeedColors(
    val seed: Int,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
) {
    object Blue : SeedColors(
        seed = 0xFF0061A4.toInt(),
        primary = Color(0xFF2EC7FF),
        secondary = Color(0xFF688D8F),
        tertiary = Color(0xFF674875)
    )

    object Indigo : SeedColors(
        seed = 0xFF535A92.toInt(),
        primary = Color(0xFFA9C7FF),
        secondary = Color(0xFF8891A5),
        tertiary = Color(0xFF856079)
    )

    object Purple : SeedColors(
        seed = 0xFF794A99.toInt(),
        primary = Color(0xFFD6BBFB),
        secondary = Color(0xFF978CA3),
        tertiary = Color(0xFFD49DA7)
    )

    object Pink : SeedColors(
        seed = 0xFF94416F.toInt(),
        primary = Color(0xFFF7B1DE),
        secondary = Color(0xFFA58999),
        tertiary = Color(0xFFD69F86)
    )

    object Red : SeedColors(
        seed = 0xFFFF3B30.toInt(),
        primary = Color(0xFFFFB4A7),
        secondary = Color(0xFFAE8882),
        tertiary = Color(0xFFC1A973)
    )

    object Orange : SeedColors(
        seed = 0xFF855318.toInt(),
        primary = Color(0xFFFDB975),
        secondary = Color(0xFFA88B71),
        tertiary = Color(0xFFA5B080)
    )

    object Yellow : SeedColors(
        seed = 0xFF785A0B.toInt(),
        primary = Color(0xFFEBC97D),
        secondary = Color(0xFFA08F6E),
        tertiary = Color(0xFF96B38F)
    )

    object Teal : SeedColors(
        seed = 0xFF666014.toInt(),
        primary = Color(0xFFD2C973),
        secondary = Color(0xFF969270),
        tertiary = Color(0xFF8BB49E)
    )

    object Green : SeedColors(
        seed = 0xFF1E6B4F.toInt(),
        primary = Color(0xFF8CD5B4),
        secondary = Color(0xFF7E9689),
        tertiary = Color(0xFF8BB1C3)
    )
}