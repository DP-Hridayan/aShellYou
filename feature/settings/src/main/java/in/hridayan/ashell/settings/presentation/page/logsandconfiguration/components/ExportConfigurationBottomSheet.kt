@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.logsandconfiguration.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.scrollBottomHaptic
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.ToastUtils

private val SHEET_HORIZONTAL_PADDING = 24.dp
private val SHEET_BOTTOM_PADDING = 32.dp
private val SPACING_SMALL = 10.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private val PREVIEW_MAX_HEIGHT = 320.dp
private val PREVIEW_CORNER_RADIUS = 12.dp
private val PREVIEW_INNER_PADDING = 14.dp
private val PROGRESS_INDICATOR_SIZE = 36.dp

@Composable
fun ExportConfigurationBottomSheet(
    configPreview: String,
    isLoading: Boolean,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
) {
    val context = LocalContext.current
    val res = LocalResources.current

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SHEET_HORIZONTAL_PADDING)
                .padding(bottom = SHEET_BOTTOM_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.export_configuration),
                    style = MaterialTheme.typography.headlineSmall
                )

                if (configPreview.isNotBlank()) {
                    IconButton(
                        onClick = withHaptic {
                            ClipboardUtils.copyToClipboard(
                                text = configPreview,
                                context = context
                            ) {
                                ToastUtils.makeToast(
                                    context,
                                    res.getString(R.string.copied_to_clipboard)
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = stringResource(R.string.copy),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SPACING_SMALL))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.export_configuration_preview_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(SPACING_MEDIUM))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = PREVIEW_MAX_HEIGHT)
                    .scrollBottomHaptic()
                    .clip(RoundedCornerShape(PREVIEW_CORNER_RADIUS))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(PREVIEW_INNER_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(PROGRESS_INDICATOR_SIZE),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    val scrollState = rememberScrollState()
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            text = configPreview,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SPACING_LARGE))

            val buttonGroupItems = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                    text = stringResource(R.string.cancel),
                    iconResId = R.drawable.ic_cancel,
                    onClick = onDismiss,
                ),
                ButtonGroupItem(
                    text = stringResource(R.string.export),
                    iconResId = R.drawable.ic_share,
                    enabled = !isLoading && !isExporting && configPreview.isNotBlank(),
                    onClick = onExport,
                ),
            )

            OverflowButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                items = buttonGroupItems,
            )
        }
    }
}
