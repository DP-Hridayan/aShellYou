package `in`.hridayan.ashell.adbsideload.presentation.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.modifier.dashedBorder
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadFileCard(
    fileName: String?,
    fileSize: Long,
    onPickFile: () -> Unit,
    onClearFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (fileName == null) {
        EmptyFileCard(onPickFile = onPickFile, modifier = modifier)
    } else {
        SelectedFileCard(
            fileName = fileName,
            fileSize = fileSize,
            onClearFile = onClearFile,
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyFileCard(
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .dashedBorder(
                strokeWidth = 1.dp,
                cornerRadius = 24.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            .clickable { onPickFile() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.select_zip_package),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.tap_to_browse),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedFileCard(
    fileName: String,
    fileSize: Long,
    onClearFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            FileInfo(fileName = fileName, fileSize = fileSize, modifier = Modifier.weight(1f))
            IconButton(onClick = onClearFile) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FileInfo(
    fileName: String,
    fileSize: Long,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (fileSize > 0L) {
            Text(
                text = formatFileSize(fileSize),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun formatFileSize(bytes: Long): String {
    val locale = LocalLocale.current

    return when {
        bytes >= 1_073_741_824L -> String.format(
            locale.platformLocale,
            "%.2f GB",
            bytes / 1_073_741_824.0
        )

        bytes >= 1_048_576L -> String.format(
            locale.platformLocale, "%.1f MB",
            bytes / 1_048_576.0
        )

        bytes >= 1024L -> String.format(
            locale.platformLocale,
            "%.0f KB", bytes / 1024.0
        )

        else -> "$bytes B"
    }
}
