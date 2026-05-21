package `in`.hridayan.settingsdsl.model

/**
 * A type-safe identifier for a custom composable slot within a settings page.
 *
 * Subclass this to define your own named slots. Each slot is rendered by the
 * `customSlotContent` lambda you provide to `settingsContent()`.
 *
 * Example:
 * ```kotlin
 * object AppSlots {
 *     object ColorPicker : CustomSlot("color_picker")
 *     object GoogleSignIn : CustomSlot("google_sign_in")
 *     object LastBackupInfo : CustomSlot("last_backup_info")
 * }
 * ```
 *
 * Usage in `settingsPage`:
 * ```kotlin
 * settingsPage(
 *     customSlot(AppSlots.GoogleSignIn),
 *     group(...),
 * )
 * ```
 */
abstract class CustomSlot(val id: String)
