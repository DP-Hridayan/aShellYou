@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.effect.KeepScreenOn
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.modifier.dashedBorder
import `in`.hridayan.ashell.core.presentation.components.slidetoconfirm.SlideToConfirm
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionRisk
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionValidationResult
import `in`.hridayan.ashell.shell.fastboot.domain.validation.PartitionValidator
import `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog.CustomPartitionFlashConfirmDialog
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.FlashOperationProgressContent

@Composable
fun FlashPartitionBottomSheet(
    onDismiss: () -> Unit,
    flashOperation: FlashOperation,
    onFlash: (partition: String, uri: Uri) -> Unit,
    onResetOperation: () -> Unit,
    onCancel: () -> Unit
) {
    var selectedPartition by rememberSaveable { mutableStateOf(DEFAULT_PARTITION) }
    var customPartition by rememberSaveable { mutableStateOf("") }
    var isCustomPartition by rememberSaveable { mutableStateOf(false) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var selectedFileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val isOperationRunning = flashOperation.status.isActive
    val isOperationVisible = isOperationRunning || flashOperation.status.isFinished
    val targetPartition = if (isCustomPartition) customPartition.trim() else selectedPartition
    val partitionValidation = if (isCustomPartition) {
        PartitionValidator.validate(customPartition)
    } else {
        PartitionValidationResult.Valid
    }
    val partitionRisk = PartitionValidator.assessRisk(targetPartition)
    val isPartitionValid = !isCustomPartition || partitionValidation == PartitionValidationResult.Valid
    val canSlideToFlash = selectedFileUri != null && isPartitionValid && !isOperationRunning

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = resolveDisplayName(context, it)
        }
    }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    KeepScreenOn(enabled = isOperationRunning)

    ModalBottomSheet(
        onDismissRequest = { if (!isOperationRunning) onDismiss() },
        sheetState = sheetState,
        sheetGesturesEnabled = !isOperationRunning,
        dragHandle = null,
        properties = ModalBottomSheetProperties(shouldDismissOnClickOutside = !isOperationRunning)
    ) {
        FlashPartitionContent(
            selectedPartition = selectedPartition,
            isCustomPartition = isCustomPartition,
            customPartition = customPartition,
            partitionValidation = partitionValidation,
            partitionRisk = partitionRisk,
            selectedFileName = selectedFileName,
            isFileAdded = selectedFileUri != null,
            isOperationVisible = isOperationVisible,
            flashOperation = flashOperation,
            canSlideToFlash = canSlideToFlash,
            isOperationRunning = isOperationRunning,
            onSelectCustomMode = { isCustomPartition = true },
            onSwitchToCommon = {
                isCustomPartition = false
                customPartition = ""
            },
            onCustomPartitionChange = { customPartition = it },
            onPartitionSelected = { partition ->
                selectedPartition = partition
                isCustomPartition = false
            },
            onChooseFile = withHaptic { filePickerLauncher.launch(arrayOf("*/*")) },
            onFileRemoved = withHaptic {
                selectedFileUri = null
                selectedFileName = null
            },
            onSlideToConfirm = withHaptic(HapticFeedbackType.GestureThresholdActivate) {
                val needsConfirmation =
                    isCustomPartition || partitionRisk != PartitionRisk.STANDARD
                if (needsConfirmation) {
                    showConfirmDialog = true
                } else {
                    selectedFileUri?.let { uri -> onFlash(targetPartition, uri) }
                }
            },
            onCancel = onCancel,
            onDismissOperation = onResetOperation
        )
    }

    if (showConfirmDialog && selectedFileUri != null) {
        CustomPartitionFlashConfirmDialog(
            title = stringResource(
                if (isCustomPartition) R.string.confirm_custom_partition_flash_title
                else R.string.confirm_high_risk_flash_title
            ),
            partition = targetPartition,
            fileName = selectedFileName ?: stringResource(R.string.file),
            risk = partitionRisk,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                selectedFileUri?.let { uri -> onFlash(targetPartition, uri) }
            }
        )
    }
}

@Composable
internal fun FlashPartitionContent(
    modifier: Modifier = Modifier,
    selectedPartition: String,
    isCustomPartition: Boolean,
    customPartition: String,
    partitionValidation: PartitionValidationResult,
    partitionRisk: PartitionRisk,
    selectedFileName: String?,
    isFileAdded: Boolean,
    isOperationVisible: Boolean,
    flashOperation: FlashOperation,
    canSlideToFlash: Boolean,
    isOperationRunning: Boolean,
    onSelectCustomMode: () -> Unit,
    onSwitchToCommon: () -> Unit,
    onCustomPartitionChange: (String) -> Unit,
    onPartitionSelected: (String) -> Unit,
    onChooseFile: () -> Unit,
    onFileRemoved: () -> Unit,
    onSlideToConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissOperation: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        AutoResizeableText(
            stringResource(R.string.flash_partition),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AutoResizeableText(
            stringResource(R.string.partition),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        PartitionSelector(
            isCustom = isCustomPartition,
            selectedPartition = selectedPartition,
            customPartition = customPartition,
            validationResult = partitionValidation,
            risk = partitionRisk,
            enabled = !isOperationVisible,
            onSelectCustomMode = onSelectCustomMode,
            onSwitchToCommon = onSwitchToCommon,
            onCustomPartitionChange = onCustomPartitionChange,
            onPartitionSelected = onPartitionSelected
        )

        if (isOperationVisible) {
            FlashOperationProgressContent(
                operation = flashOperation,
                completedText = stringResource(R.string.flash_complete),
                onCancel = onCancel,
                onDismiss = onDismissOperation
            )
        } else {
            ChooseFileHintBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .padding(vertical = 25.dp),
                onClick = onChooseFile,
                onFileRemoved = onFileRemoved,
                isFileAdded = isFileAdded,
                selectedFileName = selectedFileName
            )

            SlideToConfirm(
                modifier = Modifier.fillMaxWidth(),
                onConfirm = onSlideToConfirm,
                enabled = canSlideToFlash,
                confirmed = isOperationRunning,
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

private const val DEFAULT_PARTITION = "boot"

private val COMMON_PARTITIONS = listOf(
    "boot", "recovery", "system", "vendor", "dtbo",
    "vbmeta", "vbmeta_system", "vendor_boot", "init_boot",
    "super", "userdata", "cache", "metadata"
)

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String {
    val queried = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull()
    return queried ?: uri.lastPathSegment ?: context.getString(R.string.file)
}

@Composable
private fun PartitionSelector(
    isCustom: Boolean,
    selectedPartition: String,
    customPartition: String,
    validationResult: PartitionValidationResult,
    risk: PartitionRisk,
    enabled: Boolean,
    onSelectCustomMode: () -> Unit,
    onSwitchToCommon: () -> Unit,
    onCustomPartitionChange: (String) -> Unit,
    onPartitionSelected: (String) -> Unit,
) {
    if (isCustom) {
        CustomPartitionField(
            customPartition = customPartition,
            validationResult = validationResult,
            risk = risk,
            enabled = enabled,
            onCustomPartitionChange = onCustomPartitionChange,
            onSwitchToCommon = onSwitchToCommon
        )
    } else {
        CommonPartitionDropdown(
            selectedPartition = selectedPartition,
            enabled = enabled,
            onPartitionSelected = onPartitionSelected,
            onSelectCustomMode = onSelectCustomMode
        )
    }
}

@Composable
private fun CommonPartitionDropdown(
    selectedPartition: String,
    enabled: Boolean,
    onPartitionSelected: (String) -> Unit,
    onSelectCustomMode: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedPartition,
            onValueChange = {},
            label = { Text(stringResource(R.string.partition)) },
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            COMMON_PARTITIONS.forEach { partition ->
                DropdownMenuItem(
                    text = { Text(text = partition, fontFamily = FontFamily.Monospace) },
                    onClick = {
                        onPartitionSelected(partition)
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.custom), fontStyle = FontStyle.Italic) },
                onClick = {
                    expanded = false
                    onSelectCustomMode()
                }
            )
        }
    }
}

@Composable
private fun CustomPartitionField(
    customPartition: String,
    validationResult: PartitionValidationResult,
    risk: PartitionRisk,
    enabled: Boolean,
    onCustomPartitionChange: (String) -> Unit,
    onSwitchToCommon: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = customPartition,
            onValueChange = onCustomPartitionChange,
            label = { Text(stringResource(R.string.custom_partition_name)) },
            placeholder = { Text(stringResource(R.string.custom_partition_hint)) },
            enabled = enabled,
            singleLine = true,
            isError = validationResult is PartitionValidationResult.InvalidFormat ||
                validationResult is PartitionValidationResult.TooLong,
            supportingText = {
                when (validationResult) {
                    PartitionValidationResult.Blank,
                    PartitionValidationResult.Valid -> Unit
                    PartitionValidationResult.InvalidFormat -> {
                        Text(stringResource(R.string.custom_partition_invalid_format))
                    }
                    PartitionValidationResult.TooLong -> {
                        Text(stringResource(R.string.custom_partition_too_long))
                    }
                }
            },
            trailingIcon = {
                if (customPartition.isNotEmpty()) {
                    IconButton(onClick = withHaptic { onCustomPartitionChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = withHaptic { onSwitchToCommon() }) {
                Text(stringResource(R.string.custom_partition_switch_to_common))
            }
        }

        if (customPartition.isNotBlank()) {
            CustomPartitionWarningCard(risk = risk)
        }
    }
}

@Composable
private fun CustomPartitionWarningCard(risk: PartitionRisk) {
    val isCritical = risk == PartitionRisk.CRITICAL
    val containerColor = if (isCritical) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (isCritical) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }
    val message = if (isCritical) {
        stringResource(R.string.critical_partition_warning)
    } else {
        stringResource(R.string.custom_partition_warning_desc)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

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
                painter = if (isFileAdded) {
                    painterResource(R.drawable.ic_description)
                } else {
                    painterResource(R.drawable.ic_note_add)
                },
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )

            if (isFileAdded) {
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
