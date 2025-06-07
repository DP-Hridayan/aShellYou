package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.presentation.components.dialog.CustomDialog
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@Composable
fun EditCommandDialog(
    modifier: Modifier = Modifier.Companion,
    onDismiss: () -> Unit,
    id : Int,
    viewModel: CommandViewModel = hiltViewModel()
) {

    val command by viewModel.command.collectAsState()
    val description by viewModel.description.collectAsState()
    val label by viewModel.label.collectAsState()

    val commandError by viewModel.commandError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()

    CustomDialog(
        onDismiss = onDismiss, content = {
            Column(
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                DialogTitle(
                    text = stringResource(R.string.add_command),
                    modifier = Modifier.Companion
                        .padding(vertical = Dimens.paddingLarge)
                        .align(Alignment.Companion.CenterHorizontally)
                )

                CommandInputField(
                    command, viewModel::onCommandChange, commandError, modifier = Modifier.Companion
                )

                DescriptionInputField(
                    description,
                    viewModel::onDescriptionChange,
                    descriptionError,
                    modifier = Modifier.Companion
                )

                LabelInputField(
                    label, viewModel::onLabelChange, modifier = Modifier.Companion
                )

                Row(
                    modifier = Modifier.Companion.padding(
                        top = Dimens.paddingLarge,
                        start = Dimens.paddingLarge,
                        end = Dimens.paddingLarge
                    ), horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
                ) {
                    CancelButton(
                        onClick = onDismiss, modifier = Modifier.Companion.weight(1f)
                    )

                    UpdateButton(
                        id = id,
                        viewModel = viewModel,
                        onSuccess = onDismiss,
                        modifier = Modifier.Companion.weight(1f)
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError
    )
}

@Composable
private fun LabelInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    val label = stringResource(R.string.label)

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) }
    )
}

@Composable
fun UpdateButton(
    id:Int,
    viewModel: CommandViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    Button(
        modifier = modifier, onClick = {
            viewModel.editCommand (id = id) {
                onSuccess()
            }
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

