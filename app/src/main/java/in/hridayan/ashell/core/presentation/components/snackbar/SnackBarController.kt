package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import `in`.hridayan.ashell.core.common.LocalSnackBarController

/**
 * Controller that manages the currently displayed [SnackBarEvent].
 *
 * Intended to be provided app-wide via [LocalSnackBarController].
 * Showing a new snackbar while one is visible immediately replaces it.
 */
@Stable
class SnackBarController {

    var currentEvent by mutableStateOf<SnackBarEvent?>(null)
        private set

    /** Show a simple auto-dismiss snackbar. */
    fun show(
        message: String,
        durationMillis: Int = 3000,
    ) {
        show(SnackBarEvent.Simple(message = message.clean(), durationMillis = durationMillis))
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
            SnackBarEvent.WithAction(
                message = message.clean(),
                durationMillis = durationMillis,
                actionText = actionText,
                onActionClicked = onActionClicked,
                onDismiss = onDismiss,
            )
        )
    }

    /** Show any [SnackBarEvent], replacing any currently visible one. */
    fun show(event: SnackBarEvent) {
        // Invoke dismiss callback of the currently shown event before replacing
        (currentEvent as? SnackBarEvent.WithAction)?.onDismiss?.invoke()
        currentEvent = event
    }

    /** Clear the current snackbar, invoking its onDismiss if applicable. */
    fun dismiss() {
        (currentEvent as? SnackBarEvent.WithAction)?.onDismiss?.invoke()
        currentEvent = null
    }

    /** Clears without invoking onDismiss (used after action is clicked). */
    internal fun clearSilently() {
        currentEvent = null
    }

    private fun String.clean() = trim().removeSurrounding("\"").trim()
}
