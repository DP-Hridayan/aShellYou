package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile

@Composable
fun FileInfoDialog(
    file: RemoteFile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (file.isDirectory) "Folder Info" else "File Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Name", file.name)
                InfoRow("Path", file.path)
                InfoRow("Type", if (file.isDirectory) "Folder" else "File")
                if (!file.isDirectory && file.displaySize.isNotEmpty()) {
                    InfoRow("Size", file.displaySize)
                }
                if (file.lastModified.isNotEmpty()) {
                    InfoRow("Modified", file.lastModified)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
