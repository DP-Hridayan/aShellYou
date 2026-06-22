package `in`.hridayan.ashell.core.presentation.components.snackbar

/**
 * Represents a snackbar event to be shown.
 *
 * - [Simple]: message only, auto-dismisses after [durationMillis].
 * - [WithAction]: message + action button with optional dismiss callback.
 */
sealed class SnackbarEvent {

    abstract val message: String
    abstract val durationMillis: Int

    data class Simple(
        override val message: String,
        override val durationMillis: Int = 3000,
    ) : SnackbarEvent()

    data class WithAction(
        override val message: String,
        override val durationMillis: Int = 3000,
        val actionText: String,
        val onActionClicked: () -> Unit,
        val onDismiss: (() -> Unit)? = null,
    ) : SnackbarEvent()
}
