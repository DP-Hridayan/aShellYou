package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import `in`.hridayan.ashell.core.resources.R

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

@Composable
fun ForgetDeviceConfirmationDialog(
    modifier: Modifier = Modifier,
    deviceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val res = LocalResources.current
    val message = res.getString(R.string.forget_device_confirmation, deviceName)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.forget_device),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OverflowButtonGroup(
                    items = listOf(
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                            text = stringResource(R.string.cancel),
                            onClick = { onDismiss() }
                        ),
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ),
                            text = stringResource(R.string.forget),
                            onClick = {
                                onConfirm()
                                onDismiss()
                            }
                        )
                    )
                )
            }
        }
    }
}
