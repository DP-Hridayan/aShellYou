package `in`.hridayan.ashell.core.presentation.theme.domain.model

object PresetColorSchemes {
    const val PRESET_ASHELLYOU_SPECIAL_ID = -1

    val AShellYouSpecial = UserGeneratedColorScheme(
        id = PRESET_ASHELLYOU_SPECIAL_ID,
        name = "aShellYou Special",

        // Primary (Forest Bamboo Leaf)
        primary = "#1A835C",
        onPrimary = "#FFFFFF",
        primaryContainer = "#C5F6DD",
        onPrimaryContainer = "#003823",
        inversePrimary = "#61DFA3",

        // Secondary (Morning Fog Sky Celadon)
        secondary = "#237E90",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#D0F3F9",
        onSecondaryContainer = "#00363F",

        // Tertiary (Amber Dew)
        tertiary = "#8E6E0F",
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FFF0B8",
        onTertiaryContainer = "#352800",

        // Error (Cherry Blossom Dawn)
        error = "#C83B60",
        onError = "#FFFFFF",
        errorContainer = "#FFE1E7",
        onErrorContainer = "#420015",

        // Surface & Background
        background = "#F5F9F6",
        onBackground = "#141E17",
        surface = "#FAFDFB",
        onSurface = "#141E17",
        surfaceVariant = "#D7E5DD",
        onSurfaceVariant = "#3D4A41",
        surfaceTint = "#1A835C",
        inverseSurface = "#253128",
        inverseOnSurface = "#EAF3EC",
        surfaceBright = "#FAFDFB",
        surfaceDim = "#D4DCD5",
        surfaceContainer = "#E8F0EA",
        surfaceContainerHigh = "#E2EBE4",
        surfaceContainerHighest = "#DCE4DD",
        surfaceContainerLow = "#EFF6F0",
        surfaceContainerLowest = "#FFFFFF",

        // Misc
        outline = "#6A766D",
        outlineVariant = "#BFCCC1",
        scrim = "#000000",
        isDarkTheme = false,
        createdAt = Long.MAX_VALUE
    )

    val allPresets: List<UserGeneratedColorScheme> = listOf(AShellYouSpecial)

    fun getPresetById(id: Int): UserGeneratedColorScheme? = allPresets.find { it.id == id }

    fun isPreset(id: Int): Boolean = id < 0
}
