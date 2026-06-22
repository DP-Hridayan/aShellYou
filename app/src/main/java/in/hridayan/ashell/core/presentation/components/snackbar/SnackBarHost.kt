package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.core.presentation.utils.SnackBarUtils
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SnackBarHost() {
    val data = SnackBarUtils.snackbarData
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data != null) {
            visible = false
            delay(30.milliseconds)
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
