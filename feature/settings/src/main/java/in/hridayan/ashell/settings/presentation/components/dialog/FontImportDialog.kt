@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.FontStyleBottomSheet
import `in`.hridayan.ashell.settings.presentation.model.FontImportState
import java.io.File

@Composable
fun FontImportDialog(
    state: FontImportState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (state !is FontImportState.Saving) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = state !is FontImportState.Saving)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                when (state) {
                    is FontImportState.InvalidFile -> InvalidFileContent(onDismiss = onDismiss)
                    is FontImportState.NamingPrompt -> NamingPromptContent(
                        prefilledName = state.prefilledName,
                        tempFilePath = state.tempFilePath,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss
                    )

                    is FontImportState.Saving -> SavingContent()
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun InvalidFileContent(onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        AutoResizeableText(
            text = stringResource(R.string.invalid_font_file),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.invalid_font_file_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    OverflowButtonGroup(
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(),
                text = stringResource(R.string.dismiss),
                onClick = { onDismiss() }
            )
        )
    )
}

@Composable
private fun NamingPromptContent(
    prefilledName: String,
    tempFilePath: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(prefilledName) { mutableStateOf(prefilledName) }

    // Local format toggles — independent of the bottom sheet's ViewModel state
    var isUppercase by remember { mutableStateOf(false) }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }

    val previewFontFamily = remember(tempFilePath) {
        if (tempFilePath.isNotEmpty()) {
            runCatching { FontFamily(Font(File(tempFilePath))) }.getOrNull()
        } else null
    } ?: FontFamily.Default

    val previewText = stringResource(R.string.font_display_text)
    val displayText = if (isUppercase) previewText.uppercase() else previewText

    AutoResizeableText(
        text = stringResource(R.string.import_font),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(16.dp))

    AutoResizeableText(
        text = stringResource(R.string.preview),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            text = displayText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = previewFontFamily,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
            )
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    FontPreviewFormatRow(
        isUppercase = isUppercase,
        isBold = isBold,
        isItalic = isItalic,
        isUnderline = isUnderline,
        onToggleUppercase = { isUppercase = !isUppercase },
        onToggleBold = { isBold = !isBold },
        onToggleItalic = { isItalic = !isItalic },
        onToggleUnderline = { isUnderline = !isUnderline },
        onClear = {
            isUppercase = false
            isBold = false
            isItalic = false
            isUnderline = false
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = name,
        onValueChange = { name = it },
        label = { Text(stringResource(R.string.import_font_name_hint)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ),
                enabled = name.isNotBlank(),
                text = stringResource(R.string.action_import),
                onClick = { onConfirm(name.trim()) }
            )
        )
    )
}

/**
 * A compact format toggle row matching the style of [TextFormatUtilityRow] in
 * [FontStyleBottomSheet], but driven by local state rather than the ViewModel.
 */
@Composable
private fun FontPreviewFormatRow(
    isUppercase: Boolean,
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    onToggleUppercase: () -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val uncheckedContainerColor = BottomSheetDefaults.ContainerColor
    val uncheckedContentColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = BottomSheetDefaults.ContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            FormatToggleCell(
                modifier = Modifier.weight(1f),
                checked = isUppercase,
                checkedContainerColor = checkedContainerColor,
                uncheckedContainerColor = uncheckedContainerColor,
                onClick = withHaptic(HapticFeedbackType.VirtualKey) { onToggleUppercase() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_matchcase),
                    contentDescription = null,
                    tint = if (isUppercase) checkedContentColor else uncheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            FormatToggleCell(
                modifier = Modifier.weight(1f),
                checked = isBold,
                checkedContainerColor = checkedContainerColor,
                uncheckedContainerColor = uncheckedContainerColor,
                onClick = withHaptic(HapticFeedbackType.VirtualKey) { onToggleBold() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_bold),
                    contentDescription = null,
                    tint = if (isBold) checkedContentColor else uncheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            FormatToggleCell(
                modifier = Modifier.weight(1f),
                checked = isItalic,
                checkedContainerColor = checkedContainerColor,
                uncheckedContainerColor = uncheckedContainerColor,
                onClick = withHaptic(HapticFeedbackType.VirtualKey) { onToggleItalic() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_italic),
                    contentDescription = null,
                    tint = if (isItalic) checkedContentColor else uncheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            FormatToggleCell(
                modifier = Modifier.weight(1f),
                checked = isUnderline,
                checkedContainerColor = checkedContainerColor,
                uncheckedContainerColor = uncheckedContainerColor,
                onClick = withHaptic(HapticFeedbackType.VirtualKey) { onToggleUnderline() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_underline),
                    contentDescription = null,
                    tint = if (isUnderline) checkedContentColor else uncheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClear() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_clear),
                    contentDescription = null,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun FormatToggleCell(
    checked: Boolean,
    checkedContainerColor: androidx.compose.ui.graphics.Color,
    uncheckedContainerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (checked) checkedContainerColor else uncheckedContainerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
private fun SavingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = stringResource(R.string.import_font),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
