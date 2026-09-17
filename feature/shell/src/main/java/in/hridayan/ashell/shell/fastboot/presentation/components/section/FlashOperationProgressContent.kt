package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.ashell.shell.fastboot.domain.policy.FlashCancelPolicy
import `in`.hridayan.ashell.shell.fastboot.domain.policy.FlashCancellation
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.CancelTransferDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.text.flashStatusText

/**
 * Progress and outcome view shared by the flash and wipe sheets.
 *
 * Cancelling is offered only while it means something. Once the device is writing a partition the
 * host cannot stop it, so the control is replaced by a warning not to disconnect.
 */
@Composable
fun FlashOperationProgressContent(
    operation: FlashOperation,
    completedText: String,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingCancel by remember { mutableStateOf(false) }

    val isActive = operation.status.isActive
    val policy = FlashCancellation.policyFor(operation.status)

    val animatedProgress by animateFloatAsState(
        targetValue = operation.progress,
        animationSpec = tween(PROGRESS_ANIMATION_MS),
        label = "progress"
    )
    val statusColor by animateColorAsState(targetValue = statusColor(operation.status), label = "statusColor")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = statusIcon(operation.status),
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FileName(fileName = operation.fileName)

        StatusMessage(
            text = flashStatusText(operation, completedText),
            color = statusColor
        )

        if (isActive) {
            ProgressBar(progress = operation.progress, animatedProgress = animatedProgress, color = statusColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isActive) {
            ActiveControls(
                policy = policy,
                onCancel = onCancel,
                onRequestConfirmation = { confirmingCancel = true }
            )
        }

        if (operation.status.isFinished) {
            DismissButton(status = operation.status, onDismiss = onDismiss)
        }
    }

    if (confirmingCancel) {
        CancelTransferDialog(
            onDismiss = { confirmingCancel = false },
            onConfirm = {
                confirmingCancel = false
                onCancel()
            }
        )
    }
}

@Composable
private fun ActiveControls(
    policy: FlashCancelPolicy,
    onCancel: () -> Unit,
    onRequestConfirmation: () -> Unit,
) {
    when (policy) {
        FlashCancelPolicy.IMMEDIATE -> CancelButton(onCancel = onCancel)
        FlashCancelPolicy.CONFIRM -> CancelButton(onCancel = onRequestConfirmation)
        FlashCancelPolicy.UNSAFE -> DoNotDisconnectWarning()
    }
}

@Composable
private fun DoNotDisconnectWarning() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.do_not_disconnect),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FileName(fileName: String) {
    if (fileName.isBlank()) return
    Text(
        text = fileName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun StatusMessage(text: String, color: Color) {
    if (text.isBlank()) return
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = color,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun statusColor(status: FlashStatus): Color = when (status) {
    FlashStatus.ERROR, FlashStatus.CANCELLED -> MaterialTheme.colorScheme.error
    FlashStatus.COMPLETE -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private fun statusIcon(status: FlashStatus): ImageVector = when (status) {
    FlashStatus.COMPLETE -> Icons.Default.CheckCircle
    FlashStatus.ERROR, FlashStatus.CANCELLED -> Icons.Default.Error
    else -> Icons.Default.FlashOn
}

@Composable
private fun ProgressBar(progress: Float, animatedProgress: Float, color: Color) {
    val barModifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
    if (progress > 0f) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = barModifier,
            color = color,
            trackColor = color.copy(alpha = TRACK_ALPHA),
        )
        Text(
            text = "${(progress * PERCENT).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        LinearProgressIndicator(
            modifier = barModifier,
            color = color,
            trackColor = color.copy(alpha = TRACK_ALPHA),
        )
    }
}

@Composable
private fun CancelButton(onCancel: () -> Unit) {
    OutlinedButton(
        onClick = onCancel,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.cancel))
    }
}

@Composable
private fun DismissButton(status: FlashStatus, onDismiss: () -> Unit) {
    val isSuccess = status == FlashStatus.COMPLETE
    Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(if (isSuccess) R.string.done else R.string.close))
    }
}

private const val PROGRESS_ANIMATION_MS = 300
private const val TRACK_ALPHA = 0.15f
private const val PERCENT = 100
