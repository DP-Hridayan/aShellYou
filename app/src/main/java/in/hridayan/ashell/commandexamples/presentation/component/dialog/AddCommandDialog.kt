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
import `in`.hridayan.ashell.commandexamples.presentation.model.InputFieldState
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.theme.Dimens

@Composable
fun AddCommandDialog(
    onDismiss: () -> Unit,
    viewModel: CommandViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val states by viewModel.states.collectAsState()

    DialogContainer(
        onDismiss = onDismiss
    ) {
        DialogTitle(
            text = stringResource(R.string.add_command),
            modifier = Modifier
                .padding(bottom = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        CommandInputField(
            onValueChange = viewModel::onCommandFieldTextChange,
            state = states.commandField
        )

        DescriptionInputField(
            onValueChange = viewModel::onDescriptionFieldTextChange,
            state = states.descriptionField
        )

        if (states.labelField.labels.isNotEmpty()) {
            Labels(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                labels = states.labelField.labels,
                showCrossIcon = true
            )
        }

        LabelInputField()

        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.paddingLarge)
        ) {
            OutlinedButton(
                onClick = {
                    weakHaptic()
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
                onClick = {
                    weakHaptic()
                    viewModel.addCommand { onDismiss() }
                },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[1]),
                interactionSource = interactionSources[1],
                shapes = ButtonDefaults.shapes(),
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.add),
                )
            }
        }
    }
}

@Composable
private fun CommandInputField(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    state: InputFieldState.CommandInputFieldState
) {
    val label =
        if (state.isError) state.errorMessage else stringResource(R.string.command)

    OutlinedTextField(
        value = state.fieldText,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = state.isError,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun DescriptionInputField(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    state: InputFieldState.DescriptionInputFieldState
) {
    val label =
        if (state.isError) state.errorMessage else stringResource(R.string.description)

    OutlinedTextField(
        value = state.fieldText,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = state.isError,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun LabelInputField(
    modifier: Modifier = Modifier,
    viewModel: CommandViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val state by viewModel.states.collectAsState()

    val label =
        if (state.labelField.isError) state.labelField.errorMessage else stringResource(R.string.label)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = state.labelField.fieldText,
        isError = state.labelField.isError,
        onValueChange = { viewModel.onLabelFieldTextChange(it) },
        trailingIcon = {
            IconButton(
                onClick = {
                    weakHaptic()
                    viewModel.onLabelAdd(state.labelField.fieldText)
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
