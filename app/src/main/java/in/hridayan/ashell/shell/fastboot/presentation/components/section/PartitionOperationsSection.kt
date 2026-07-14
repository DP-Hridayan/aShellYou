@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext

private val COMMON_PARTITIONS = listOf(
    "boot", "recovery", "system", "vendor", "dtbo",
    "vbmeta", "vbmeta_system", "vendor_boot", "init_boot",
    "super", "userdata", "cache", "metadata"
)

@Composable
fun PartitionOperationsSection(
    isConnected: Boolean,
    flashOperation: FlashOperation,
    onFlash: (partition: String, uri: Uri) -> Unit,
    onErase: (partition: String) -> Unit,
    onBootImage: (uri: Uri) -> Unit,
    onResetOperation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPartition by rememberSaveable { mutableStateOf("boot") }
    var customPartition by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var showEraseConfirm by rememberSaveable { mutableStateOf(false) }
    var selectedFileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val isOperationRunning = flashOperation.status in listOf(
        FlashStatus.READING_FILE, FlashStatus.DOWNLOADING, FlashStatus.FLASHING, FlashStatus.ERASING
    )

    val targetPartition = customPartition.ifBlank { selectedPartition }

    // File picker — resolve real file name via ContentResolver
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = try {
                context.contentResolver.query(it, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
            } catch (_: Exception) { null } ?: it.lastPathSegment ?: "selected file"
        }
    }

    // Boot image picker
    val bootImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onBootImage(it) }
    }

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingExtraLarge)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SdStorage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.fastboot_partitions),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Partition selector
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { if (!isOperationRunning) dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = customPartition.ifBlank { selectedPartition },
                    onValueChange = { customPartition = it },
                    label = { Text("Partition") },
                    readOnly = customPartition.isBlank(),
                    enabled = !isOperationRunning,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    COMMON_PARTITIONS.forEach { partition ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = partition,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            onClick = {
                                selectedPartition = partition
                                customPartition = ""
                                dropdownExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Custom…",
                                fontStyle = FontStyle.Italic
                            )
                        },
                        onClick = {
                            customPartition = ""
                            dropdownExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // File selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    enabled = !isOperationRunning && isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedFileName ?: "Select image file…",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                if (selectedFileUri != null) {
                    IconButton(
                        onClick = {
                            selectedFileUri = null
                            selectedFileName = null
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Flash button
                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        selectedFileUri?.let { uri ->
                            onFlash(targetPartition, uri)
                        }
                    },
                    enabled = isConnected && selectedFileUri != null && !isOperationRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.flash_partition))
                }

                // Erase button
                FilledTonalButton(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        showEraseConfirm = true
                    },
                    enabled = isConnected && !isOperationRunning,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.erase_partition))
                }

                // Boot image button
                FilledTonalButton(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        bootImagePickerLauncher.launch(arrayOf("*/*"))
                    },
                    enabled = isConnected && !isOperationRunning
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.boot_image))
                }
            }
        }
    }

    // Erase confirmation dialog
    if (showEraseConfirm) {
        AlertDialog(
            onDismissRequest = { showEraseConfirm = false },
            title = {
                Text(
                    text = "Erase $targetPartition?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "This will permanently erase the $targetPartition partition. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEraseConfirm = false
                        onErase(targetPartition)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Erase")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEraseConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

