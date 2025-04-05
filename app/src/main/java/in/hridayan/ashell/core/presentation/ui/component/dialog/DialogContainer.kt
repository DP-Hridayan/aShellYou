package `in`.hridayan.ashell.core.presentation.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.core.presentation.ui.theme.Shape

@Composable
fun CustomDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip((Shape.cardCornerLarge))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(Dimens.paddingLarge)
        ) {
            content()
        }
    }
}