package `in`.hridayan.ashell.settings.domain.model

/**
 * Represents a locale that the app supports for in-app language switching.
 *
 * @param tag BCP 47 language tag (e.g. "fr-FR"). Empty string means "System default".
 * @param displayName The locale's name in the current app language (e.g. "French (France)").
 * @param nativeName The locale's name in its own language (e.g. "Français (France)").
 */
data class SupportedLocale(
    val tag: String,
    val displayName: String,
    val nativeName: String,
    val translationProgress: Int? = null,
)
