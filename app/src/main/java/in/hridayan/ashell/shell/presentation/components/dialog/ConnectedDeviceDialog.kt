@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.dialog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun ConnectedDeviceDialog(
    modifier: Modifier = Modifier,
    connectedDevice: String?,
    showModeSwitchButton: Boolean = true,
    onDismiss: () -> Unit
) {
    var showExpandedLayout by rememberSaveable { mutableStateOf(false) }

    var rotationAngle by rememberSaveable { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "Spin"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .widthIn(min = 280.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                AutoResizeableText(
                    text = stringResource(R.string.connected_device),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                ConnectedDeviceCard(connectedDevice = connectedDevice)

                if (!showModeSwitchButton) return@Column

                Button(
                    onClick = withHaptic {
                        showExpandedLayout = !showExpandedLayout
                        rotationAngle -= 360f
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(animatedRotation)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AutoResizeableText(
                        text = stringResource(R.string.switch_mode),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (showExpandedLayout) {
                    ExpandedLayoutView(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(modifier: Modifier = Modifier, connectedDevice: String?) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.largeIncreased)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.largeIncreased
            )
            .clickable(onClick = withHaptic { }),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = connectedDevice ?: stringResource(R.string.none),
            style = MaterialTheme.typography.titleSmallEmphasized,
            maxLines = 1,
            modifier = Modifier
                .padding(15.dp)
                .basicMarquee(),
        )
    }
}

@Composable
private fun ExpandedLayoutView(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
) {
    val key = SettingsKeys.LOCAL_ADB_WORKING_MODE

    val initialSelected =
        settingsViewModel.getInt(key = key).collectAsState(initial = key.default as Int)
    var selected by rememberSaveable { mutableIntStateOf(initialSelected.value) }

    LaunchedEffect(initialSelected.value) {
        selected = initialSelected.value
    }

    val items = RadioGroupOptionsProvider.localAdbShellModeOptions

    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = withHaptic {
                            selected = option.value
                        }
                    )
            ) {
                Text(
                    text = stringResource(option.labelResId),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f))

                RadioButton(
                    selected = (option.value == selected),
                    onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                        selected = option.value
                    }
                )
            }
        }

        @Suppress("DEPRECATION")
        ButtonGroup(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = withHaptic(HapticFeedbackType.Reject) {
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                interactionSource = interactionSources[0],
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onDismiss()
                    settingsViewModel.setInt(key = key, value = selected)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[1]),
                interactionSource = interactionSources[1],
                shapes = ButtonDefaults.shapes(),
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.confirm),
                )
            }
        }
    }
}