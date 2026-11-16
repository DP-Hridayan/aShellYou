package `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionRisk

@Composable
fun CustomPartitionFlashConfirmDialog(
    title: String,
    partition: String,
    fileName: String,
    risk: PartitionRisk,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val isCritical = risk == PartitionRisk.CRITICAL
    val canConfirm = !isCritical || acknowledged

    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            AutoResizeableText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.confirm_custom_partition_flash_message, partition, fileName),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (isCritical) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.critical_partition_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = acknowledged,
                        onCheckedChange = { acknowledged = it }
                    )
                    Text(
                        text = stringResource(R.string.critical_partition_ack),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = withHaptic(HapticFeedbackType.Reject) { onDismiss() }
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) { onConfirm() },
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.flash_partition))
                }
            }
        }
    }
}
