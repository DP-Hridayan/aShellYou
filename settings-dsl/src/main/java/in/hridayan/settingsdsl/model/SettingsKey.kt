package `in`.hridayan.settingsdsl.model

/**
 * A type-safe identifier for a settings entry.
 *
 * Implement this interface in your own sealed class to define
 * the keys used in your settings pages.
 *
 * Example:
 * ```kotlin
 * sealed class AppKey<out T>(
 *     override val name: String,
 *     override val defaultValue: T,
 * ) : SettingsKey<T> {
 *     data object DynamicColors : AppKey<Boolean>("dynamic_colors", true)
 *     data object DarkTheme    : AppKey<Int>("dark_theme", -1)
 * }
 * ```
 *
 * @param T the type of the default value this key carries.
 */
interface SettingsKey<out T> {
    val name: String
    val defaultValue: T
}
