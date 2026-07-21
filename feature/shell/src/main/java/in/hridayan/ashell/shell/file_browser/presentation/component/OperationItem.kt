package `in`.hridayan.ashell.shell.file_browser.presentation.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType

@Composable
fun OperationItem(
    operation: FileOperation,
    onCancel: () -> Unit
) {
    val progress = if (operation.totalBytes > 0) {
        operation.bytesTransferred.toFloat() / operation.totalBytes.toFloat()
    } else 0f

    val icon = when (operation.type) {
        OperationType.DOWNLOAD -> Icons.Rounded.Download
        OperationType.UPLOAD -> Icons.Rounded.Upload
        OperationType.COPY -> Icons.Rounded.ContentCopy
        OperationType.MOVE -> Icons.AutoMirrored.Rounded.DriveFileMove
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = operation.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (operation.totalBytes > 0) {
                Text(
                    text = "${formatSize(operation.bytesTransferred)} / ${formatSize(operation.totalBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
