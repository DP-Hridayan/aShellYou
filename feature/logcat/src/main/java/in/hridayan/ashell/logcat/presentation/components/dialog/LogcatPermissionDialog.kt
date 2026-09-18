@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.logcat.presentation.components.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undraw404Error
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R

/**
 * Shown when Log access mode is selected but READ_LOGS is not granted.
 *
 * READ_LOGS cannot be requested through a system dialog, so the ADB grant
 * command is shown in a selectable, copyable code block. Start stays blocked
 * until [onGranted] confirms the permission is present.
 */
@Composable
fun LogcatPermissionDialog(
    grantCommand: String,
    onCopyCommand: () -> Unit,
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp),
            ) {
                Image(
                    imageVector = DynamicColorImageVectors.undraw404Error(),
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                AutoResizeableText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.logcat_permission_title),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.logcat_permission_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                GrantCommandBlock(command = grantCommand, onCopy = onCopyCommand)

                Spacer(modifier = Modifier.height(24.dp))

                OverflowButtonGroup(
                    items = listOf(
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                            text = stringResource(R.string.cancel),
                            onClick = onDismiss,
                        ),
                        ButtonGroupItem(
                            text = stringResource(R.string.logcat_ive_granted_it),
                            onClick = onGranted,
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun GrantCommandBlock(
    command: String,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = command,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        IconButton(onClick = onCopy) {
            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = stringResource(R.string.copy),
            )
        }
    }
}
