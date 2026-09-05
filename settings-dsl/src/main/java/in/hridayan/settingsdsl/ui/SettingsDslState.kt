package `in`.hridayan.settingsdsl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import `in`.hridayan.settingsdsl.dsl.SettingsGraphBuilder

/**
 * Holds the key of the settings item that should be scrolled to and highlighted.
 *
 * The [activeKey] can be any type — it is matched directly against the `key` parameter
 * passed to item builders in [SettingsGraphBuilder].
 *
 * Obtain an instance via [rememberSettingsDslState] and access it through [LocalSettingsDslState].
 */
@Stable
class HighlightState internal constructor(initialKey: String? = null) {
    /**
     * The normalised key of the item to highlight, or null if no highlight is active.
     *
     * Keys are stored as `key.toString()`, the same normalisation the renderer applies to lazy
     * list item keys, so the state stays saveable regardless of the key type callers use.
     * [SettingsColumn] clears this once it has claimed the highlight.
     */
    var activeKey: String? by mutableStateOf(initialKey)
        internal set

    /**
     * Programmatically activates a highlight for the item identified by [key].
     *
     * Call this before navigating to the screen that contains the target item.
     *
     * @param key The key of the item to highlight. Must match the key passed to the item builder.
     */
    fun highlight(key: Any) {
        activeKey = key.toString()
    }

    internal fun clear() {
        activeKey = null
    }

    internal companion object {
        val Saver: Saver<HighlightState, String> = Saver(
            save = { it.activeKey.orEmpty() },
            restore = { HighlightState(it.ifEmpty { null }) },
        )
    }
}

/**
 * Top-level state object provided by [LocalSettingsDslState].
 *
 * Carries both the active [HighlightState] and the global [OnClickDefaults] that
 * [SettingsColumn] falls back to when no per-item override is set.
 *
 * Create instances via [rememberSettingsDslState].
 *
 * @param highlightState The highlight/scroll-to state shared across all settings screens.
 * @param onClickDefaults Global default callbacks for switch items, boolean reads, and int reads.
 */
@Stable
class SettingsDslState(
    val highlightState: HighlightState,
    val onClickDefaults: OnClickDefaults,
)

/**
 * [CompositionLocal] that provides the active [SettingsDslState].
 *
 * Must be provided above the [NavHost] using
 * [CompositionLocalProvider]. Accessing this local without providing
 * it will throw an [IllegalStateException] with a descriptive message.
 */
val LocalSettingsDslState = compositionLocalOf<SettingsDslState> {
    error(
        "LocalSettingsDslState not provided. Wrap your NavHost with " +
            "CompositionLocalProvider(LocalSettingsDslState provides rememberSettingsDslState())"
    )
}

/**
 * Creates and remembers a [SettingsDslState] instance.
 *
 * Provide the returned instance to [LocalSettingsDslState] above your
 * [NavHost]. Use the [onClickDefaults] block to register global default
 * callbacks for switch items, boolean state reads, and integer state reads. These defaults are
 * overridden on a per-item basis via the item builder DSL.
 *
 * Example:
 * ```kotlin
 * val dslState = rememberSettingsDslState {
 *     onSwitchItem  { key -> viewModel.onToggle(key as SettingsKeys<Boolean>) }
 *     isChecked     { key -> prefs[booleanPreferencesKey((key as SettingsKeys<*>).name)] ?: false }
 *     selectedValue { key -> prefs[intPreferencesKey((key as SettingsKeys<*>).name)] ?: -1 }
 * }
 * CompositionLocalProvider(LocalSettingsDslState provides dslState) {
 *     NavHost(navController = navController, ...) { ... }
 * }
 * ```
 *
 * @param onClickDefaults Builder block for registering global default callbacks.
 */
@Composable
fun rememberSettingsDslState(
    vararg keys: Any?,
    onClickDefaults: OnClickDefaultsScope.() -> Unit = {},
): SettingsDslState {
    val highlightState = rememberSaveable(saver = HighlightState.Saver) { HighlightState() }
    val defaults = remember(*keys) { OnClickDefaultsScope().apply(onClickDefaults).build() }
    return remember(defaults) { SettingsDslState(highlightState, defaults) }
}
