package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.dialog.CustomDialog
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@Composable
fun EditCommandDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    id: Int,
    viewModel: CommandViewModel = hiltViewModel()
) {

    val command by viewModel.command.collectAsState()
    val description by viewModel.description.collectAsState()
    val labels by viewModel.labels.collectAsState()

    val commandError by viewModel.commandError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()

    CustomDialog(
        modifier = modifier,
        onDismiss = onDismiss,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogTitle(
                    text = stringResource(R.string.edit_command),
                    modifier = Modifier
                        .padding(vertical = Dimens.paddingMedium)
                        .align(Alignment.Companion.CenterHorizontally)
                )

                CommandInputField(
                    command, viewModel::onCommandChange, commandError, modifier = Modifier
                )

                DescriptionInputField(
                    description,
                    viewModel::onDescriptionChange,
                    descriptionError,
                    modifier = Modifier
                )

                if (labels.isNotEmpty()) {
                    Labels(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 10.dp),
                        labels = labels,
                        showCrossIcon = true
                    )
                }

                LabelInputField()

                Row(
                    modifier = Modifier.padding(
                        top = Dimens.paddingLarge,
                        start = Dimens.paddingLarge,
                        end = Dimens.paddingLarge
                    ), horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
                ) {
                    CancelButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f)
                    )

                    UpdateButton(
                        id = id,
                        viewModel = viewModel,
                        onSuccess = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        })

}

@Composable
private fun CommandInputField(
    value: String, onValueChange: (String) -> Unit, isError: Boolean, modifier: Modifier
) {
    val label =
        if (isError) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.command)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError
    )
}

@Composable
private fun DescriptionInputField(
    value: String, onValueChange: (String) -> Unit, isError: Boolean, modifier: Modifier
) {
    val label =
        if (isError) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.description)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError
    )
}

@Composable
private fun LabelInputField(
    modifier: Modifier = Modifier,
    viewModel: CommandViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    var value by remember { mutableStateOf("") }
    val isError by viewModel.labelError.collectAsState()
    val label =
        if (isError) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.label)


    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        isError = isError,
        onValueChange = {
            value = it
            viewModel.clearLabelError()
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    weakHaptic()
                    viewModel.onLabelAdd(value)
                    value = ""
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
            }
        },
        label = { Text(label) }
    )
}


@Composable
fun UpdateButton(
    id: Int,
    viewModel: CommandViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier, onClick = {
            viewModel.editCommand(id = id) { onSuccess() }
        }) {
        Text(
            text = stringResource(R.string.update), style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = modifier, onClick = onClick
    ) {
        Text(
            text = stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge
        )
    }
}

