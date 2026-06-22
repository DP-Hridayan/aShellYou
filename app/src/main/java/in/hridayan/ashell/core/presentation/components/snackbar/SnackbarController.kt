package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Controller that manages the currently displayed [SnackbarEvent].
 *
 * Intended to be provided app-wide via [LocalSnackbarController].
 * Showing a new snackbar while one is visible immediately replaces it.
 */
@Stable
class SnackbarController {

    var currentEvent by mutableStateOf<SnackbarEvent?>(null)
        private set

    /** Show a simple auto-dismiss snackbar. */
    fun show(
        message: String,
        durationMillis: Int = 3000,
    ) {
        show(SnackbarEvent.Simple(message = message.clean(), durationMillis = durationMillis))
    }

    /** Show a snackbar with an action button. */
    fun show(
        message: String,
        actionText: String,
        durationMillis: Int = 3000,
        onActionClicked: () -> Unit,
        onDismiss: (() -> Unit)? = null,
    ) {
        show(
            SnackbarEvent.WithAction(
                message = message.clean(),
                durationMillis = durationMillis,
                actionText = actionText,
                onActionClicked = onActionClicked,
                onDismiss = onDismiss,
            )
        )
    }

    /** Show any [SnackbarEvent], replacing any currently visible one. */
    fun show(event: SnackbarEvent) {
        // Invoke dismiss callback of the currently shown event before replacing
        (currentEvent as? SnackbarEvent.WithAction)?.onDismiss?.invoke()
        currentEvent = event
    }

    /** Clear the current snackbar, invoking its onDismiss if applicable. */
    fun dismiss() {
        (currentEvent as? SnackbarEvent.WithAction)?.onDismiss?.invoke()
        currentEvent = null
    }

    /** Clears without invoking onDismiss (used after action is clicked). */
    internal fun clearSilently() {
        currentEvent = null
    }

    private fun String.clean() = trim().removeSurrounding("\"").trim()
}
