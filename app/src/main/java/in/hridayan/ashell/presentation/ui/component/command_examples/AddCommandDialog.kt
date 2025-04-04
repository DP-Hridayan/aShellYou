package `in`.hridayan.ashell.presentation.ui.component.command_examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.presentation.ui.component.dialog.CustomDialog
import `in`.hridayan.ashell.presentation.ui.component.text.DialogTitle
import `in`.hridayan.ashell.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.presentation.viewmodel.CommandViewModel

@Composable
fun AddCommandDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: CommandViewModel = hiltViewModel()
) {

    val command by viewModel.command.collectAsState()
    val description by viewModel.description.collectAsState()
    val example by viewModel.example.collectAsState()

    val commandError by viewModel.commandError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()
    val exampleError by viewModel.exampleError.collectAsState()

    CustomDialog(
        onDismiss = onDismiss, content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogTitle(
                    text = stringResource(R.string.add_command),
                    modifier = Modifier
                        .padding(vertical = Dimens.paddingLarge)
                        .align(Alignment.CenterHorizontally)
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

                ExampleInputField(
                    example, viewModel::onExampleChange, exampleError, modifier = Modifier
                )

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

                    AddButton(
                        viewModel = viewModel, onSuccess = onDismiss, modifier = Modifier.weight(1f)
                    )
                }
            }
        })
}

@Composable
fun CommandInputField(
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError
    )
}

@Composable
fun DescriptionInputField(
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError
    )
}

@Composable
fun ExampleInputField(
    value: String, onValueChange: (String) -> Unit, isError: Boolean, modifier: Modifier
) {
    val label =
        if (isError) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.example)

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError
    )
}

@Composable
fun AddButton(viewModel: CommandViewModel, onSuccess: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier, onClick = {
            viewModel.addCommand {
                onSuccess()
            }
        }) {
        Text(
            text = stringResource(R.string.add), style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = modifier, onClick = onClick
    ) {
        Text(
            text = stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge
        )
    }
}
