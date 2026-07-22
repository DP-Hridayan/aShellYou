@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet


import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.modifier.dashedBorder
import `in`.hridayan.ashell.core.presentation.components.slidetoconfirm.SlideToConfirm
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus

@Composable
fun FlashPartitionBottomSheet(
    onDismiss: () -> Unit,
    isConnected: Boolean,
    flashOperation: FlashOperation,
    onFlash: (partition: String, uri: Uri) -> Unit,
    onErase: (partition: String) -> Unit,
    onBootImage: (uri: Uri) -> Unit,
    onResetOperation: () -> Unit,
    onCancel: () -> Unit
) {

    var selectedPartition by rememberSaveable { mutableStateOf("boot") }
    var customPartition by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var showEraseConfirm by rememberSaveable { mutableStateOf(false) }
    var selectedFileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }

    var isFlashSliderGestureConfirmed by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val isOperationRunning = flashOperation.status in listOf(
        FlashStatus.READING_FILE, FlashStatus.DOWNLOADING, FlashStatus.FLASHING, FlashStatus.ERASING
    )
    val isOperationFinished = flashOperation.status in listOf(
        FlashStatus.COMPLETE, FlashStatus.ERROR
    )

    val targetPartition = customPartition.ifBlank { selectedPartition }

    // Trigger flash when slider is confirmed
    androidx.compose.runtime.LaunchedEffect(isFlashSliderGestureConfirmed) {
        if (isFlashSliderGestureConfirmed) {
            selectedFileUri?.let { uri ->
                onFlash(targetPartition, uri)
            }
        }
    }

    // File picker — resolve real file name via ContentResolver
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = try {
                context.contentResolver.query(
                    it,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
            } catch (_: Exception) {
                null
            } ?: it.lastPathSegment ?: "selected file"
        }
    }

    // Boot image picker
    val bootImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onBootImage(it) }
    }


    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!isOperationRunning) onDismiss()
        },
        sheetState = sheetState,
        sheetGesturesEnabled = !isOperationRunning,
        dragHandle = null,
        properties = ModalBottomSheetProperties(shouldDismissOnClickOutside = !isOperationRunning)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            AutoResizeableText(
                stringResource(R.string.flash_partition),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Partition selector — always visible
            AutoResizeableText(
                stringResource(R.string.partition),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = {
                    if (!isOperationRunning && !isOperationFinished) dropdownExpanded = it
                }
            ) {
                OutlinedTextField(
                    value = customPartition.ifBlank { selectedPartition },
                    onValueChange = { customPartition = it },
                    label = { Text("Partition") },
                    readOnly = customPartition.isBlank(),
                    enabled = !isOperationRunning && !isOperationFinished,
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

            // Conditional content: file picker + slide OR flashing progress
            if (isOperationRunning || isOperationFinished) {
                // Flashing progress UI
                FlashingProgressContent(
                    operation = flashOperation,
                    onCancel = onCancel,
                    onDismiss = {
                        onResetOperation()
                        isFlashSliderGestureConfirmed = false
                    }
                )
            } else {
                // File picker + slide to flash
                ChooseFileHintBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .padding(vertical = 25.dp),
                    onClick = withHaptic { filePickerLauncher.launch(arrayOf("*/*")) },
                    onFileRemoved = withHaptic {
                        isFlashSliderGestureConfirmed = false
                        selectedFileUri = null
                        selectedFileName = null
                    },
                    isFileAdded = selectedFileUri != null,
                    selectedFileName = selectedFileName
                )

                SlideToConfirm(
                    modifier = Modifier.fillMaxWidth(),
                    onConfirm = withHaptic(HapticFeedbackType.GestureThresholdActivate) {
                        isFlashSliderGestureConfirmed = true
                    },
                    enabled = selectedFileUri != null,
                    confirmed = isFlashSliderGestureConfirmed,
                    initialText = stringResource(R.string.slide_to_flash),
                    finalText = stringResource(R.string.flashing),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    thumbContainerColor = MaterialTheme.colorScheme.error,
                    thumbContentColor = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

private val COMMON_PARTITIONS = listOf(
    "boot", "recovery", "system", "vendor", "dtbo",
    "vbmeta", "vbmeta_system", "vendor_boot", "init_boot",
    "super", "userdata", "cache", "metadata"
)

@Composable
private fun ChooseFileHintBox(
    modifier: Modifier = Modifier,
    selectedFileName: String? = null,
    isFileAdded: Boolean = false,
    cornerRadius: Dp = 24.dp,
    onClick: () -> Unit = {},
    onFileRemoved: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .dashedBorder(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                strokeWidth = 2.dp,
                cornerRadius = cornerRadius
            )
            .clickable(enabled = !isFileAdded, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp),
                painter = if (isFileAdded) painterResource(R.drawable.ic_description)
                else painterResource(R.drawable.ic_note_add),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )

            if (isFileAdded)
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable(enabled = true, onClick = onFileRemoved)
                ) {
                    Icon(
                        modifier = Modifier.padding(3.dp),
                        painter = painterResource(R.drawable.ic_cross),
                        tint = MaterialTheme.colorScheme.onError,
                        contentDescription = null
                    )
                }
        }

        if (isFileAdded && selectedFileName != null) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = selectedFileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(R.string.des_select_file_for_flashing),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FlashingProgressContent(
    operation: FlashOperation,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val isActive = operation.status in listOf(
        FlashStatus.READING_FILE, FlashStatus.DOWNLOADING,
        FlashStatus.FLASHING, FlashStatus.ERASING, FlashStatus.CANCELLING
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
            else -> MaterialTheme.colorScheme.primary
        },
        label = "statusColor"
    )

    val statusIcon = when (operation.status) {
        FlashStatus.COMPLETE -> Icons.Default.CheckCircle
        FlashStatus.ERROR -> Icons.Default.Error
        else -> Icons.Default.FlashOn
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
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

        // File name
        if (operation.fileName.isNotBlank()) {
            Text(
                text = operation.fileName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Status message
        if (operation.message.isNotBlank()) {
            Text(
                text = operation.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = statusColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

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

                Text(
                    text = "${(operation.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = statusColor,
                    modifier = Modifier.padding(top = 8.dp)
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        if (isActive) {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
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
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (operation.status == FlashStatus.COMPLETE)
                        stringResource(R.string.done)
                    else
                        stringResource(R.string.close)
                )
            }
        }
    }
}
