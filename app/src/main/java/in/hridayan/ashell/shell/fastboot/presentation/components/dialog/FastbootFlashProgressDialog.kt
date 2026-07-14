@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus

@Composable
fun FastbootFlashProgressDialog(
    operation: FlashOperation,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val isActive = operation.status in listOf(
        FlashStatus.READING_FILE,
        FlashStatus.DOWNLOADING,
        FlashStatus.FLASHING,
        FlashStatus.ERASING,
        FlashStatus.CANCELLING
    )
    val isFinished = operation.status in listOf(FlashStatus.COMPLETE, FlashStatus.ERROR)

    val animatedProgress by animateFloatAsState(
        targetValue = operation.progress,
        animationSpec = tween(300),
        label = "progress"
    )

    val statusColor by animateColorAsState(
        targetValue = when (operation.status) {
            FlashStatus.ERROR -> MaterialTheme.colorScheme.error
            FlashStatus.COMPLETE -> MaterialTheme.colorScheme.tertiary
            FlashStatus.CANCELLING -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.primary
        },
        label = "statusColor"
    )

    val statusIcon = when (operation.status) {
        FlashStatus.COMPLETE -> Icons.Default.CheckCircle
        FlashStatus.ERROR -> Icons.Default.Error
        FlashStatus.ERASING -> Icons.Default.DeleteSweep
        FlashStatus.CANCELLING -> Icons.Default.Cancel
        else -> Icons.Default.FlashOn
    }

    val titleText = when (operation.status) {
        FlashStatus.READING_FILE -> stringResource(R.string.reading_file)
        FlashStatus.DOWNLOADING -> stringResource(R.string.downloading)
        FlashStatus.FLASHING -> stringResource(R.string.flashing)
        FlashStatus.ERASING -> stringResource(R.string.erasing)
        FlashStatus.CANCELLING -> stringResource(R.string.cancelling)
        FlashStatus.COMPLETE -> stringResource(R.string.operation_complete)
        FlashStatus.ERROR -> stringResource(R.string.error)
        else -> ""
    }

    Dialog(
        onDismissRequest = { if (isFinished) onDismiss() },
        properties = DialogProperties(
            dismissOnClickOutside = isFinished,
            dismissOnBackPress = isFinished
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .widthIn(min = 300.dp, max = 400.dp)
                .heightIn(min = 200.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status icon
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = statusColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Partition info
                if (operation.partition.isNotBlank()) {
                    Text(
                        text = operation.partition,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // File name
                if (operation.fileName.isNotBlank()) {
                    Text(
                        text = operation.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress bar
                if (isActive) {
                    if (operation.progress > 0f) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.15f),
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.15f),
                        )
                    }

                    // Percentage
                    if (operation.progress > 0f) {
                        Text(
                            text = "${(operation.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            color = statusColor,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .alpha(0.8f)
                        )
                    }
                }

                // Status message
                if (operation.message.isNotBlank()) {
                    Text(
                        text = operation.message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.cancel))
                        }
                    }

                    if (isFinished) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (operation.status == FlashStatus.COMPLETE)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }
    }
}
