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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel

@Composable
fun RebootOptionsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val interactionSources1 = remember { List(2) { MutableInteractionSource() } }
    val interactionSources2 = remember { List(2) { MutableInteractionSource() } }
    val buttonSize = 110.dp
    val iconSize = 36.dp

    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = modifier
                .padding(24.dp)
                .widthIn(min = 280.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            @Suppress("DEPRECATION")
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                IconButton(
                    onClick = withHaptic {
                        shellViewModel.executeSimpleCommand(arrayOf("su", "-c", "reboot"))
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
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                IconButton(
                    onClick = withHaptic {
                        shellViewModel.executeSimpleCommand(
                            arrayOf(
                                "su",
                                "-c",
                                "reboot recovery"
                            )
                        )
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
                        painter = painterResource(R.drawable.ic_restart_recovery),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
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
                    text = stringResource(R.string.system),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                AutoResizeableText(
                    text = stringResource(R.string.recovery),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            @Suppress("DEPRECATION")
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                IconButton(
                    onClick = withHaptic {
                        shellViewModel.executeSimpleCommand(
                            arrayOf(
                                "su",
                                "-c",
                                "reboot bootloader"
                            )
                        )
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
                        painter = painterResource(R.drawable.ic_restart_bootloader),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                IconButton(
                    onClick = withHaptic {
                        shellViewModel.executeSimpleCommand(
                            arrayOf(
                                "su",
                                "-c",
                                "reboot fastboot"
                            )
                        )
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
                        painter = painterResource(R.drawable.ic_restart_fastboot),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
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
                    text = stringResource(R.string.bootloader),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                AutoResizeableText(
                    text = stringResource(R.string.fastboot),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}