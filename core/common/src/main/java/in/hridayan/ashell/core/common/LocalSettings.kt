package `in`.hridayan.ashell.core.common

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal providing access to the app's settings.
 *
 * Returns a [SettingsState] instance that supports granular per-key reads
 * via bracket syntax:
 *
 * ```kotlin
 * val settings = LocalSettings.current
 * val haptics = settings[SettingsKeys.HapticsAndVibration]
 * ```
 *
 * Uses `staticCompositionLocalOf` because the [SettingsState] instance itself
 * is stable — it wraps the ViewModel and never changes identity. The
 * per-key granularity comes from each `collectAsState()` inside [SettingsState.get].
 */
val LocalSettings = staticCompositionLocalOf<SettingsState> {
    error("No SettingsState provided — ensure CompositionLocals { } wraps your content")
}