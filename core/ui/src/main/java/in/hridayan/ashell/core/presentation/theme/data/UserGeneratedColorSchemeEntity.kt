package `in`.hridayan.ashell.core.presentation.theme.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "custom_themes")
data class UserGeneratedColorSchemeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,

    // Primary
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val inversePrimary: String,

    // Secondary
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,

    // Tertiary
    val tertiary: String,
    val onTertiary: String,
    val tertiaryContainer: String,
    val onTertiaryContainer: String,

    // Error
    val error: String,
    val onError: String,
    val errorContainer: String,
    val onErrorContainer: String,

    // Surface
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val surfaceTint: String,
    val inverseSurface: String,
    val inverseOnSurface: String,
    val surfaceBright: String,
    val surfaceDim: String,
    val surfaceContainer: String,
    val surfaceContainerHigh: String,
    val surfaceContainerHighest: String,
    val surfaceContainerLow: String,
    val surfaceContainerLowest: String,

    // Misc
    val outline: String,
    val outlineVariant: String,
    val scrim: String,
    val svgPathData: String = "",

    val isDarkTheme: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)

fun ColorSchemePayload.toEntity(): UserGeneratedColorSchemeEntity {
    return UserGeneratedColorSchemeEntity(
        name = this.name,
        primary = this.primary,
        onPrimary = this.onPrimary,
        primaryContainer = this.primaryContainer,
        onPrimaryContainer = this.onPrimaryContainer,
        inversePrimary = this.inversePrimary,
        secondary = this.secondary,
        onSecondary = this.onSecondary,
        secondaryContainer = this.secondaryContainer,
        onSecondaryContainer = this.onSecondaryContainer,
        tertiary = this.tertiary,
        onTertiary = this.onTertiary,
        tertiaryContainer = this.tertiaryContainer,
        onTertiaryContainer = this.onTertiaryContainer,
        error = this.error,
        onError = this.onError,
        errorContainer = this.errorContainer,
        onErrorContainer = this.onErrorContainer,
        background = this.background,
        onBackground = this.onBackground,
        surface = this.surface,
        onSurface = this.onSurface,
        surfaceVariant = this.surfaceVariant,
        onSurfaceVariant = this.onSurfaceVariant,
        surfaceTint = this.surfaceTint,
        inverseSurface = this.inverseSurface,
        inverseOnSurface = this.inverseOnSurface,
        surfaceBright = this.surfaceBright,
        surfaceDim = this.surfaceDim,
        surfaceContainer = this.surfaceContainer,
        surfaceContainerHigh = this.surfaceContainerHigh,
        surfaceContainerHighest = this.surfaceContainerHighest,
        surfaceContainerLow = this.surfaceContainerLow,
        surfaceContainerLowest = this.surfaceContainerLowest,
        outline = this.outline,
        outlineVariant = this.outlineVariant,
        scrim = this.scrim,
        svgPathData = this.svgPathData,
        isDarkTheme = this.isDarkTheme
    )
}

fun UserGeneratedColorSchemeEntity.toPayload(): ColorSchemePayload {
    return ColorSchemePayload(
        name = this.name,
        primary = this.primary,
        onPrimary = this.onPrimary,
        primaryContainer = this.primaryContainer,
        onPrimaryContainer = this.onPrimaryContainer,
        inversePrimary = this.inversePrimary,
        secondary = this.secondary,
        onSecondary = this.onSecondary,
        secondaryContainer = this.secondaryContainer,
        onSecondaryContainer = this.onSecondaryContainer,
        tertiary = this.tertiary,
        onTertiary = this.onTertiary,
        tertiaryContainer = this.tertiaryContainer,
        onTertiaryContainer = this.onTertiaryContainer,
        error = this.error,
        onError = this.onError,
        errorContainer = this.errorContainer,
        onErrorContainer = this.onErrorContainer,
        background = this.background,
        onBackground = this.onBackground,
        surface = this.surface,
        onSurface = this.onSurface,
        surfaceVariant = this.surfaceVariant,
        onSurfaceVariant = this.onSurfaceVariant,
        surfaceTint = this.surfaceTint,
        inverseSurface = this.inverseSurface,
        inverseOnSurface = this.inverseOnSurface,
        surfaceBright = this.surfaceBright,
        surfaceDim = this.surfaceDim,
        surfaceContainer = this.surfaceContainer,
        surfaceContainerHigh = this.surfaceContainerHigh,
        surfaceContainerHighest = this.surfaceContainerHighest,
        surfaceContainerLow = this.surfaceContainerLow,
        surfaceContainerLowest = this.surfaceContainerLowest,
        outline = this.outline,
        outlineVariant = this.outlineVariant,
        scrim = this.scrim,
        svgPathData = this.svgPathData,
        isDarkTheme = this.isDarkTheme
    )
}
