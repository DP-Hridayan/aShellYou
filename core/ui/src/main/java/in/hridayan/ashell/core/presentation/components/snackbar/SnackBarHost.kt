package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.core.common.LocalSnackBarController

/**
 * Renders the app-wide snackbar driven by [LocalSnackBarController].
 *
 * Place this composable once at the root of the app (in MainActivity), overlaid
 * on top of all content. Positioning is left to the caller via [modifier].
 */
@Composable
fun SnackBarHost(modifier: Modifier = Modifier) {
    val controller = LocalSnackBarController.current
    val event = controller.currentEvent

    AnimatedSnackBar(
        modifier = modifier,
        event = event,
        onActionClicked = {
            (event as? SnackBarEvent.WithAction)?.onActionClicked?.invoke()
            controller.clearSilently()
        },
        onDismiss = {
            controller.dismiss()
        },
    )
}
