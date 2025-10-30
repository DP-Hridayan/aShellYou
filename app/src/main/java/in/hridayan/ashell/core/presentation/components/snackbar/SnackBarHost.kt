package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.core.presentation.utils.SnackBarUtils
import kotlinx.coroutines.delay

@Composable
fun SnackBarHost() {
    val data = SnackBarUtils.snackbarData
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data != null) {
            visible = false
            delay(30)
            visible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = visible && data != null) {
            data?.let { d ->
                AnimatedSnackBar(
                    message = d.message,
                    actionText = d.actionText,
                    durationMillis = d.durationMillis,
                    onActionClicked = {
                        d.onActionClicked()
                        visible = false
                        SnackBarUtils.clear()
                    },
                    onDismiss = {
                        visible = false
                        SnackBarUtils.clear()
                    }
                )
            }
        }
    }
}
