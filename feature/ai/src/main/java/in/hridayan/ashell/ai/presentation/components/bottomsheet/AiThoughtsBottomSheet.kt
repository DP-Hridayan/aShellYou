@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.presentation.components.animatedcomposable.AnimatedStopIcon
import `in`.hridayan.ashell.ai.presentation.model.Thought
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R

@Composable
fun AiThoughtsBottomSheet(
    onDismiss: () -> Unit,
    thoughts: List<Thought>,
    isGenerating: Boolean = false
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AutoResizeableText(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            text = stringResource(R.string.ai_chat_thoughts),
            style = MaterialTheme.typography.titleLarge,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            itemsIndexed(thoughts) { index, thought ->
                val isLastInList = index == thoughts.lastIndex
                val isAnimatedSpinner = isLastInList && isGenerating

                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // Left side: Icon + Vertical Line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (isAnimatedSpinner) {
                            AnimatedStopIcon(modifier = Modifier.size(24.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (!isLastInList) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }

                    // Right side: Title & Output
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = if (isLastInList) 0.dp else 24.dp)
                    ) {
                        Text(
                            text = thought.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = thought.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}