@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

@Composable
fun AiSessionOptionsBottomSheet(
    onDismiss: () -> Unit,
    session: ChatSessionEntity,
    onPin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

            ListItem(
                modifier = Modifier.clickable(
                    onClick = withHaptic { onPin() }
                ),
                content = {
                    Text(
                        if (session.isPinned) stringResource(R.string.ai_chat_unpin)
                        else stringResource(R.string.ai_chat_pin)
                    )
                },
                leadingContent = { Icon(Icons.Default.PushPin, contentDescription = null) },
            )

            ListItem(
                modifier = Modifier.clickable(
                    onClick = withHaptic { onRename() }
                ),
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                content = { Text(stringResource(R.string.ai_chat_rename)) },
            )

            ListItem(
                modifier = Modifier.clickable(
                    onClick = withHaptic { onDelete() }
                ),
                content = { Text(stringResource(R.string.ai_chat_delete)) },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}