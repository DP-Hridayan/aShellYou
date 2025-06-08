@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.home.presentation.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun RebootOptionsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val interactionSources1 = remember { List(2) { MutableInteractionSource() } }
    val interactionSources2 = remember { List(2) { MutableInteractionSource() } }
    val buttonSize = 110.dp
    val weakHaptic = LocalWeakHaptic.current

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    IconButton(
                        onClick = {
                            weakHaptic()
                            shellViewModel.executeSimpleCommand("reboot")
                        },
                        modifier = Modifier
                            .size(buttonSize)
                            .animateWidth(interactionSources1[0]),
                        interactionSource = interactionSources1[0],
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(
                        onClick = {
                            weakHaptic()
                            shellViewModel.executeSimpleCommand("reboot recovery")
                        },
                        modifier = Modifier
                            .size(buttonSize)
                            .animateWidth(interactionSources1[1]),
                        interactionSource = interactionSources1[1],
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    AutoResizeableText(
                        text = "System",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    AutoResizeableText(
                        text = "Recovery",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    IconButton(
                        onClick = {
                            weakHaptic()
                            shellViewModel.executeSimpleCommand("reboot bootloader")
                        },
                        modifier = Modifier
                            .size(buttonSize)
                            .animateWidth(interactionSources2[0]),
                        interactionSource = interactionSources2[0],
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    IconButton(
                        onClick = {
                            weakHaptic()
                            shellViewModel.executeSimpleCommand("am force-stop com.android.systemui")
                        },
                        modifier = Modifier
                            .size(buttonSize)
                            .animateWidth(interactionSources2[1]),
                        interactionSource = interactionSources2[1],
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    AutoResizeableText(
                        text = "Bootloader",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    AutoResizeableText(
                        text = "System UI",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}