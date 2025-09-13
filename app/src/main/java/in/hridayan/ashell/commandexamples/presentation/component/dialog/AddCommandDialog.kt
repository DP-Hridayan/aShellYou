@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.dialog.CustomDialog
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@Composable
fun AddCommandDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: CommandViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    val command by viewModel.command.collectAsState()
    val description by viewModel.description.collectAsState()
    val label by viewModel.label.collectAsState()

    val commandError by viewModel.commandError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()

    CustomDialog(
        modifier = modifier,
        onDismiss = onDismiss, content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogTitle(
                    text = stringResource(R.string.add_command),
                    modifier = Modifier
                        .padding(bottom = Dimens.paddingLarge)
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

                LabelInputField(
                    label, viewModel::onLabelChange, modifier = Modifier
                )

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
                            viewModel.addCommand {
                                onDismiss()
                            }
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
        })
}

@Composable
private fun CommandInputField(
    value: String, onValueChange: (String) -> Unit, isError: Boolean, modifier: Modifier
) {
    val label =
        if (isError) stringResource(R.string.field_cannot_be_blank) else stringResource(R.string.command)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        modifier = modifier.fillMaxWidth()
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
private fun LabelInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    val label = stringResource(R.string.label)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) }
    )
}
