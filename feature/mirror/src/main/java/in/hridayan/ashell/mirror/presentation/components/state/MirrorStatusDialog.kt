@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.mirror.presentation.components.state

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.EndReason
import `in`.hridayan.ashell.mirror.domain.model.MirrorError

/** Why the session stopped, the server's last words when there are any, and what to do next. */
@Composable
fun MirrorFailedDialog(error: MirrorError, onRetry: () -> Unit, onLeave: () -> Unit) {
    MirrorStatusDialog(
        title = stringResource(R.string.mirror_failed_title),
        message = error.message(),
        serverLog = error.serverLog(),
        onRetry = onRetry,
        onLeave = onLeave
    )
}

@Composable
fun MirrorEndedDialog(reason: EndReason, onRetry: () -> Unit, onLeave: () -> Unit) {
    MirrorStatusDialog(
        title = stringResource(R.string.mirror_ended_title),
        message = stringResource(
            when (reason) {
                EndReason.DEVICE_DISCONNECTED -> R.string.mirror_ended_disconnected
                EndReason.SERVER_STOPPED -> R.string.mirror_ended_server_stopped
            }
        ),
        serverLog = emptyList(),
        onRetry = onRetry,
        onLeave = onLeave
    )
}

/**
 * Dismissing without choosing leaves the mirror, since there is nothing left to show behind the
 * dialog.
 */
@Composable
private fun MirrorStatusDialog(
    title: String,
    message: String,
    serverLog: List<String>,
    onRetry: () -> Unit,
    onLeave: () -> Unit
) {
    DialogContainer(
        onDismiss = onLeave,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = true)
    ) {
        AutoResizeableText(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (serverLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = serverLog.joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatusActions(onRetry = onRetry, onLeave = onLeave)
    }
}

@Composable
private fun StatusActions(onRetry: () -> Unit, onLeave: () -> Unit) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val leaveLabel = stringResource(R.string.back_to_shell)
    val retryLabel = stringResource(R.string.retry)
    val leave = withHaptic(HapticFeedbackType.Reject, onLeave)
    val retry = withHaptic(HapticFeedbackType.Confirm, onRetry)

    ButtonGroup(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.paddingLarge),
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState = menuState) }
    ) {
        customItem(
            buttonGroupContent = {
                OutlinedButton(
                    onClick = leave,
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[0],
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[0])
                ) {
                    AutoResizeableText(text = leaveLabel, style = MaterialTheme.typography.labelLarge)
                }
            },
            menuContent = {
                DropdownMenuItem(text = { AutoResizeableText(text = leaveLabel) }, onClick = leave)
            }
        )

        customItem(
            buttonGroupContent = {
                Button(
                    onClick = retry,
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[1],
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[1])
                ) {
                    AutoResizeableText(text = retryLabel, style = MaterialTheme.typography.labelLarge)
                }
            },
            menuContent = {
                DropdownMenuItem(text = { AutoResizeableText(text = retryLabel) }, onClick = retry)
            }
        )
    }
}

@Composable
private fun MirrorError.message(): String = when (this) {
    MirrorError.NotConnected -> stringResource(R.string.mirror_error_not_connected)
    MirrorError.DeployFailed -> stringResource(R.string.mirror_error_deploy_failed)
    MirrorError.ServerVersionMismatch -> stringResource(R.string.mirror_error_version)
    is MirrorError.EncoderFailed -> stringResource(R.string.mirror_error_encoder)
    is MirrorError.ServerExited -> stringResource(R.string.mirror_error_server_exited)
    is MirrorError.ConnectTimeout -> stringResource(R.string.mirror_error_connect_timeout)
    is MirrorError.DecoderUnavailable -> stringResource(R.string.mirror_error_decoder, codec.name)
    MirrorError.StreamError -> stringResource(R.string.mirror_error_stream)
    MirrorError.DeviceNotResponding -> stringResource(R.string.mirror_error_not_responding)
}

private fun MirrorError.serverLog(): List<String> = when (this) {
    is MirrorError.EncoderFailed -> serverLog
    is MirrorError.ServerExited -> serverLog
    is MirrorError.ConnectTimeout -> serverLog
    else -> emptyList()
}
