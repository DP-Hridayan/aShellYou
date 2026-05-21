package `in`.hridayan.settingsdsl.model

/**
 * A type-safe identifier for a settings entry.
 *
 * Implement this interface in your own enum or sealed class to define
 * the keys used in your settings pages.
 *
 * Example:
 * ```kotlin
 * enum class AppKey(
 *     override val name: String,
 *     override val defaultValue: Any? = null
 * ) : SettingsKey {
 *     DYNAMIC_COLORS("dynamic_colors", true),
 *     DARK_THEME("dark_theme", -1),
 * }
 * ```
 */
interface SettingsKey {
    val name: String
    val defaultValue: Any?
}
