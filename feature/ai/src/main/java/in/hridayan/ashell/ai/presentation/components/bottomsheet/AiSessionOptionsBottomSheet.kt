@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
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
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ClickableOptions(
                    shape = CardCornerShape.FIRST_CARD,
                    title = if (session.isPinned) {
                        stringResource(R.string.ai_chat_unpin)
                    } else {
                        stringResource(R.string.ai_chat_pin)
                    },
                    imageVector = ImageVector.vectorResource(R.drawable.ic_pin),
                    onClick = withHaptic { onPin() }
                )

                ClickableOptions(
                    shape = CardCornerShape.MIDDLE_CARD,
                    title = stringResource(R.string.rename),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                    onClick = withHaptic { onRename() }
                )

                ClickableOptions(
                    shape = CardCornerShape.LAST_CARD,
                    title = stringResource(R.string.delete),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                    onClick = withHaptic { onDelete() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ClickableOptions(
    modifier: Modifier = Modifier,
    shape: CustomCardShape,
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    CustomCard(
        modifier = modifier,
        shape = shape,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
            Text(text = title)
        }
    }
}
