package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.model.CmdScreenInputFieldState
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.Dimens

@Composable
fun EditCommandDialog(
    onDismiss: () -> Unit,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val states by viewModel.states.collectAsState()

    DialogContainer(
        onDismiss = onDismiss,
    ) {
        DialogTitle(
            text = stringResource(R.string.edit_command),
            modifier = Modifier
                .padding(vertical = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        CommandInputField(state = states.commandField)

        DescriptionInputField(state = states.descriptionField)

        if (states.labelField.labels.isNotEmpty()) {
            Labels(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                labels = states.labelField.labels,
                showCrossIcon = true,
                crossIconOnClick = { label -> viewModel.onLabelRemove(label) }
            )
        }

        LabelInputField()

        val buttonGroupItems = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = { onDismiss() }
            ),
            ButtonGroupItem(
                text = stringResource(R.string.update),
                onClick = { viewModel.editCommand(id = id) { onDismiss() } }
            )
        )

        OverflowButtonGroup(
            items = buttonGroupItems,
            modifier = Modifier.padding(top = Dimens.paddingLarge)
        )
    }
}

@Composable
private fun CommandInputField(
    modifier: Modifier = Modifier,
    state: CmdScreenInputFieldState.CommandInputFieldState,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val label =
        if (state.isError) state.errorMessage else stringResource(R.string.command)

    OutlinedTextField(
        value = state.fieldValue,
        onValueChange = { viewModel.onCommandFieldTextChange(it) },
        label = { Text(label) },
        isError = state.isError,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun DescriptionInputField(
    modifier: Modifier = Modifier,
    state: CmdScreenInputFieldState.DescriptionInputFieldState,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val label =
        if (state.isError) state.errorMessage else stringResource(R.string.description)

    OutlinedTextField(
        value = state.fieldValue,
        onValueChange = { viewModel.onDescriptionFieldTextChange(it) },
        label = { Text(label) },
        isError = state.isError,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun LabelInputField(
    modifier: Modifier = Modifier,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val state by viewModel.states.collectAsState()

    val label =
        if (state.labelField.isError) state.labelField.errorMessage else stringResource(R.string.label)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = state.labelField.fieldValue,
        isError = state.labelField.isError,
        onValueChange = { viewModel.onLabelFieldTextChange(it) },
        trailingIcon = {
            IconButton(
                onClick = withHaptic {
                    viewModel.onLabelAdd(state.labelField.fieldValue.text)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
            }
        },
        label = { Text(label) },
        singleLine = true
    )
}


