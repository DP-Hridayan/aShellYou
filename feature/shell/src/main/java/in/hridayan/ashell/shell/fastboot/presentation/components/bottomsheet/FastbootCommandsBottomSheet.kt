@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.presentation.components.section.FastbootCommandsSection

@Composable
fun FastbootCommandsBottomSheet(
    onDismiss: () -> Unit,
    isConnected: Boolean,
    runningCommandId: String?,
    onRunCommand: (commandId: String, command: String) -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            AutoResizeableText(
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                text = stringResource(R.string.predefined_commands),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp),
                text = stringResource(R.string.predefined_commands_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FastbootCommandsSection(
                isConnected = isConnected,
                runningCommandId = runningCommandId,
                onRunCommand = onRunCommand,
            )
        }
    }
}