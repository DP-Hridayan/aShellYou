@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.logcat.domain.model.LogEntry

/**
 * Modal bottom sheet showing the full details of a [LogEntry].
 * The message text is inside a [SelectionContainer] so the user can copy it.
 */
@Composable
fun LogEntryDetailBottomSheet(
    entry: LogEntry,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val isDark = LocalDarkMode.current
    val levelColor = LogLevelColors.barColor(entry.level)
    val textColor = LogLevelColors.textColor(entry.level, isDark)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level chip
            Surface(
                shape = RoundedCornerShape(50),
                color = levelColor.copy(alpha = 0.18f),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    text = entry.level.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Timestamp
            DetailRow(
                label = stringResource(R.string.timestamp),
                value = entry.timestamp,
            )

            // PID / TID on same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DetailRow(
                    modifier = Modifier.weight(1f),
                    label = "PID",
                    value = entry.pid,
                )
                DetailRow(
                    modifier = Modifier.weight(1f),
                    label = "TID",
                    value = entry.tid,
                )
            }

            // UID
            if (entry.uid.isNotBlank()) {
                DetailRow(
                    label = stringResource(R.string.logcat_filter_uid),
                    value = entry.uid,
                )
            }

            // Package
            if (entry.packageName.isNotBlank()) {
                DetailRow(
                    label = stringResource(R.string.logcat_detail_package),
                    value = entry.packageName,
                )
            }

            // Tag
            DetailRow(
                label = stringResource(R.string.tag),
                value = entry.tag,
                monospace = true,
            )

            // Message (selectable)
            Text(
                text = stringResource(R.string.message),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = entry.message,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    monospace: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (monospace)
                MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            else
                MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
