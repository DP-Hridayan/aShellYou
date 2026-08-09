@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.lazyselectioncontainer.LazySelectionContainer
import `in`.hridayan.lazyselectioncontainer.lazySelectionItem
import `in`.hridayan.lazyselectioncontainer.rememberLazySelectionState
import `in`.hridayan.lazyselectioncontainer.rememberLazySelectionTextLayout

/**
 * Displays the fastboot console output with a header and scrollable,
 * selectable output area. Designed to be placed in the Console tab content.
 */
@Composable
fun FastbootConsoleOutput(
    commandOutput: String,
    onClearOutput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = remember(commandOutput) {
        if (commandOutput.isBlank()) emptyList() else commandOutput.lines()
    }
    val listState = rememberLazyListState()
    val selectionState = rememberLazySelectionState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.command_console),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onClearOutput,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = stringResource(R.string.clear)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            if (lines.isEmpty()) {
                Text(
                    text = stringResource(R.string.output_will_appear_here),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazySelectionContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    selectionState = selectionState,
                    listState = listState,
                    items = lines,
                    itemToText = { it },
                    onCopy = {},
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(lines) { index, line ->
                            val textLayoutResult = rememberLazySelectionTextLayout(index)
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.lazySelectionItem(index = index),
                                onTextLayout = textLayoutResult
                            )
                        }
                    }
                }
            }
        }
    }
}
