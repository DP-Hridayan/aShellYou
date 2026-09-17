@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.FlashOperationProgressContent

data class WipeOption(
    val partition: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val isDangerous: Boolean = true
)

private val WIPE_OPTIONS = listOf(
    WipeOption(
        partition = "userdata",
        titleRes = R.string.wipe_userdata_title,
        descriptionRes = R.string.wipe_userdata_desc,
        isDangerous = true
    ),
    WipeOption(
        partition = "cache",
        titleRes = R.string.wipe_cache_title,
        descriptionRes = R.string.wipe_cache_desc,
        isDangerous = false
    ),
    WipeOption(
        partition = "metadata",
        titleRes = R.string.wipe_metadata_title,
        descriptionRes = R.string.wipe_metadata_desc,
        isDangerous = true
    )
)

@Composable
fun WipeDataBottomSheet(
    onDismiss: () -> Unit,
    eraseOperation: FlashOperation,
    onErase: (partition: String) -> Unit,
    onResetOperation: () -> Unit,
    onCancel: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    var confirmingPartition by rememberSaveable { mutableStateOf<String?>(null) }

    val isOperationRunning = eraseOperation.status.isActive
    val isOperationVisible = isOperationRunning || eraseOperation.status.isFinished

    ModalBottomSheet(
        onDismissRequest = { if (!isOperationRunning) onDismiss() },
        sheetState = sheetState,
        sheetGesturesEnabled = !isOperationRunning,
        dragHandle = null,
        properties = ModalBottomSheetProperties(shouldDismissOnClickOutside = !isOperationRunning)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            AutoResizeableText(
                stringResource(R.string.wipe_data),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isOperationVisible) {
                FlashOperationProgressContent(
                    operation = eraseOperation,
                    completedText = stringResource(R.string.erase_complete),
                    onCancel = onCancel,
                    onDismiss = onResetOperation
                )
            } else {
                WipeOptionsContent(onOptionClick = { confirmingPartition = it })
            }
        }
    }

    confirmingPartition?.let { partition ->
        EraseConfirmDialog(
            partition = partition,
            onDismiss = { confirmingPartition = null },
            onConfirm = {
                onErase(partition)
                confirmingPartition = null
            }
        )
    }
}

@Composable
private fun WipeOptionsContent(onOptionClick: (String) -> Unit) {
    Text(
        text = stringResource(R.string.wipe_data_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 20.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.wipe_warning),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error
        )
    }

    WIPE_OPTIONS.forEach { option ->
        WipeOptionItem(
            option = option,
            onClick = { onOptionClick(option.partition) }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun EraseConfirmDialog(
    partition: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            AutoResizeableText(
                text = stringResource(R.string.erase_confirm_title, partition),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(R.string.erase_confirm_message, partition),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                OutlinedButton(onClick = withHaptic(HapticFeedbackType.Reject) { onDismiss() }) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) { onConfirm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.erase_partition))
                }
            }
        }
    }
}

@Composable
private fun WipeOptionItem(
    option: WipeOption,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = withHaptic(HapticFeedbackType.Confirm) { onClick() },
        modifier = Modifier.fillMaxWidth(),
        colors = if (option.isDangerous) {
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(option.titleRes),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = stringResource(option.descriptionRes),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
