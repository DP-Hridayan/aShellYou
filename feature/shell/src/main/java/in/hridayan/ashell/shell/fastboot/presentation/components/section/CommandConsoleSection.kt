@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.hridayan.fastboot.ResponseStatus
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult

@Composable
fun CommandConsoleSection(
    commandHistory: List<FastbootCommandResult>,
    onSendCommand: (String) -> Unit,
    onClearHistory: () -> Unit,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(commandHistory.size) {
        if (commandHistory.isNotEmpty()) {
            listState.animateScrollToItem(commandHistory.lastIndex)
        }
    }

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingExtraLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.fastboot_command_console),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                if (commandHistory.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory, modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = stringResource(R.string.clear_history),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Command output area
            if (commandHistory.isNotEmpty()) {
                CustomCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 250.dp),
                    shape = CustomCardShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(commandHistory) { result ->
                            CommandOutputItem(result)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Input area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.fastboot_command_hint),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    singleLine = true,
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendCommand(inputText.trim())
                            inputText = ""
                        }
                    },
                    enabled = isConnected && inputText.isNotBlank(),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandOutputItem(result: FastbootCommandResult) {
    Column {
        // Command line
        Text(
            text = "> ${result.command}",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(0.7f)
        )

        // Response
        val color = when (result.status) {
            ResponseStatus.OKAY -> MaterialTheme.colorScheme.tertiary
            ResponseStatus.FAIL -> MaterialTheme.colorScheme.error
            ResponseStatus.INFO -> MaterialTheme.colorScheme.secondary
            ResponseStatus.DATA -> MaterialTheme.colorScheme.primary
        }

        if (result.data.isNotBlank()) {
            result.data.lines().forEach { line ->
                Row {
                    Text(
                        text = "${result.status.prefix} ",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = color
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = color
                    )
                }
            }
        } else {
            Text(
                text = result.status.prefix,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = color
            )
        }
    }
}
