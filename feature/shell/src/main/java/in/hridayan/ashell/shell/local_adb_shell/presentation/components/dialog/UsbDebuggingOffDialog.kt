@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFlexBoxApi::class)

package `in`.hridayan.ashell.shell.local_adb_shell.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R

@Composable
fun UsbDebuggingOffDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
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
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.usb_debugging_required_title),
                    style = MaterialTheme.typography.titleLargeEmphasized
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.usb_debugging_required_body),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                FlexBox(
                    modifier = Modifier.fillMaxWidth(),
                    config = {
                        direction(FlexDirection.Row)
                        wrap(FlexWrap.Wrap)
                        gap(10.dp)
                        alignItems(FlexAlignItems.Center)
                    }
                ) {
                    Button(
                        modifier = Modifier.flex { grow(1f) },
                        onClick = withHaptic { onOpenSettings() }
                    ) {
                        Text(text = stringResource(R.string.developer_options))
                    }

                    OutlinedButton(
                        modifier = Modifier.flex { grow(1f) },
                        onClick = withHaptic { onDismiss() }
                    ) {
                        Text(text = stringResource(R.string.dismiss))
                    }
                }
            }
        }
    }
}
