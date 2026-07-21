package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.common.domain.model.BackupType

@Composable
fun RestoreBackupDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    backupTime: String? = "",
    backupType: String? = ""
) {

    val (date, time) = (backupTime ?: "").split(" ").let {
        Pair(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "")
    }

    val backupTypeText = when (backupType) {
        BackupType.SETTINGS_ONLY.name -> stringResource(`in`.hridayan.ashell.core.common.R.string.settings_only)
        BackupType.DATABASE_ONLY.name -> stringResource(`in`.hridayan.ashell.core.common.R.string.databases_only)
        BackupType.SETTINGS_AND_DATABASE.name -> stringResource(`in`.hridayan.ashell.core.common.R.string.all_data)
        else -> stringResource(R.string.none)
    }

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
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.backup_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )


                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = stringResource(R.string.restore_dialog_message) + "\n" + stringResource(R.string.irreversible_action_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AutoResizeableText(
                    text = stringResource(R.string.backup_time) + " : " + time,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                AutoResizeableText(
                    text = stringResource(R.string.backup_date) + " : " + date,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                AutoResizeableText(
                    text = stringResource(R.string.backup_type) + " : " + backupTypeText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                OverflowButtonGroup(
                    items = listOf(
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(
                                type = ButtonType.OutlinedButton
                            ),
                            text = stringResource(R.string.cancel),
                            onClick = { onDismiss() }
                        ),
                        ButtonGroupItem(
                            buttonConfig = ButtonConfigDefaults.defaultConfig(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ),
                            text = stringResource(R.string.restore),
                            onClick = {
                                onConfirm()
                                onDismiss()
                            }
                        ),
                    )
                )
            }
        }
    }
}
