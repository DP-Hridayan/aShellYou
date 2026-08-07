@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

/**
 * A single predefined fastboot command entry shown in the Commands tab.
 */
data class PredefinedCommand(
    val id: String,
    val title: String,
    val description: String,
    val command: String,
    val isDangerous: Boolean = false
)

@Composable
fun FastbootCommandsSection(
    isConnected: Boolean,
    runningCommandId: String?,
    onRunCommand: (commandId: String, command: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val predefinedCommands = listOf(
        PredefinedCommand(
            id = "getvar_all",
            title = stringResource(R.string.cmd_getvar_all_title),
            description = stringResource(R.string.cmd_getvar_all_desc),
            command = "getvar:all"
        ),
        PredefinedCommand(
            id = "get_unlocked",
            title = stringResource(R.string.cmd_get_unlocked_title),
            description = stringResource(R.string.cmd_get_unlocked_desc),
            command = "getvar:unlocked"
        ),
        PredefinedCommand(
            id = "get_product",
            title = stringResource(R.string.cmd_get_product_title),
            description = stringResource(R.string.cmd_get_product_desc),
            command = "getvar:product"
        ),
        PredefinedCommand(
            id = "get_slot_count",
            title = stringResource(R.string.cmd_get_slot_count_title),
            description = stringResource(R.string.cmd_get_slot_count_desc),
            command = "getvar:slot-count"
        ),
        PredefinedCommand(
            id = "get_current_slot",
            title = stringResource(R.string.cmd_get_current_slot_title),
            description = stringResource(R.string.cmd_get_current_slot_desc),
            command = "getvar:current-slot"
        ),
        PredefinedCommand(
            id = "get_battery_level",
            title = stringResource(R.string.cmd_get_battery_title),
            description = stringResource(R.string.cmd_get_battery_desc),
            command = "getvar:battery-level"
        ),
        PredefinedCommand(
            id = "flashing_get_unlock_ability",
            title = stringResource(R.string.cmd_unlock_ability_title),
            description = stringResource(R.string.cmd_unlock_ability_desc),
            command = "flashing get_unlock_ability"
        ),
        PredefinedCommand(
            id = "oem_device_info",
            title = stringResource(R.string.cmd_oem_device_info_title),
            description = stringResource(R.string.cmd_oem_device_info_desc),
            command = "oem device-info"
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        predefinedCommands.forEach { cmd ->
            PredefinedCommandCard(
                command = cmd,
                isRunning = runningCommandId == cmd.id,
                enabled = isConnected,
                onRun = { onRunCommand(cmd.id, cmd.command) }
            )
        }
    }
}

@Composable
private fun PredefinedCommandCard(
    command: PredefinedCommand,
    isRunning: Boolean,
    enabled: Boolean,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val spinAngle by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated play/pause button
            IconButton(
                onClick = {
                    if (isRunning) {
                        onRun() // triggers cancel in VM
                    } else {
                        showConfirmDialog = true
                    }
                },
                enabled = enabled,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isRunning)
                        MaterialTheme.colorScheme.onSecondary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .size(40.dp)
                    .then(if (isRunning) Modifier.rotate(spinAngle) else Modifier)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) stringResource(R.string.cancel) else stringResource(
                        R.string.run
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = command.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isRunning)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = command.command,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                if (command.description.isNotBlank()) {
                    Text(
                        text = command.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(text = stringResource(R.string.run_command_confirm_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.run_command_confirm_message, command.title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = command.command,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showConfirmDialog = false
                        onRun()
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.run))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
