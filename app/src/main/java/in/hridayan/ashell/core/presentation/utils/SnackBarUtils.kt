package `in`.hridayan.ashell.core.presentation.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SnackBarUtils {
    internal var snackbarData by mutableStateOf<SnackBarData?>(null)
        private set

    private var isShowing = false

    data class SnackBarData(
        val message: String,
        val actionText: String,
        val durationMillis: Int = 3000,
        val onActionClicked: () -> Unit,
        val onDismiss: (() -> Unit)? = null
    )

    /**
     * Show a snackbar with a message and an action.
     * If another snackbar is already visible, it will be dismissed instantly.
     */
    fun showSnackBarWithAction(
        message: String,
        actionText: String,
        durationMillis: Int = 3000,
        onActionClicked: () -> Unit,
        onDismiss: (() -> Unit)? = null
    ) {
        // Immediately dismiss the currently showing one
        if (isShowing) {
            snackbarData?.onDismiss?.invoke()
            snackbarData = null
            isShowing = false
        }

        // Clean message: remove quotes & trim spaces
        val cleanedMessage = message.trim().removeSurrounding("\"").trim()

        snackbarData = SnackBarData(
            message = cleanedMessage,
            actionText = actionText,
            durationMillis = durationMillis,
            onActionClicked = onActionClicked,
            onDismiss = onDismiss
        )

        isShowing = true
    }

    internal fun clear() {
        snackbarData?.onDismiss?.invoke()
        snackbarData = null
        isShowing = false
    }
}
