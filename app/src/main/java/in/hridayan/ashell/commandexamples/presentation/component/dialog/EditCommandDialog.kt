@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.model.CmdScreenInputFieldState
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.theme.Dimens

@Composable
fun EditCommandDialog(
    onDismiss: () -> Unit,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val states by viewModel.states.collectAsState()


    DialogContainer(
        onDismiss = onDismiss,
    ) {
        DialogTitle(
            text = stringResource(R.string.edit_command),
            modifier = Modifier
                .padding(vertical = Dimens.paddingMedium)
                .align(Alignment.Companion.CenterHorizontally)
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

        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.paddingLarge)
        ) {
            CancelButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                interactionSource = interactionSources[0]
            )

            UpdateButton(
                id = id,
                onSuccess = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[1]),
                interactionSource = interactionSources[1]
            )
        }
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
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun LabelInputField(
    modifier: Modifier = Modifier,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
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
                onClick = {
                    weakHaptic()
                    viewModel.onLabelAdd(state.labelField.fieldValue.text)
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
    modifier: Modifier = Modifier,
    id: Int,
    onSuccess: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current

    Button(
        modifier = modifier,
        onClick = {
            weakHaptic()
            viewModel.editCommand(id = id) { onSuccess() }
        },
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSource
    ) {
        Text(
            text = stringResource(R.string.update), style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun CancelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
) {
    val weakHaptic = LocalWeakHaptic.current

    OutlinedButton(
        modifier = modifier,
        onClick = {
            weakHaptic()
            onClick()
        },
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSource
    ) {
        Text(
            text = stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge
        )
    }
}

