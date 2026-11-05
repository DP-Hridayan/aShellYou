@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.scrollBottomHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.resources.R

private val CONTENT_PADDING = 24.dp
private val SECTION_SPACING = 16.dp

@Composable
fun AddCommandBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val states by viewModel.states.collectAsState()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scrollBottomHaptic()
                .verticalScroll(scrollState)
                .padding(CONTENT_PADDING),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING)
        ) {
            AutoResizeableText(
                text = stringResource(R.string.add_command),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            CommandInputField(
                value = states.commandField.fieldValue,
                onValueChange = { viewModel.onCommandFieldTextChange(it, isNewCommand = true) },
                isError = states.commandField.isError,
                errorMessage = states.commandField.errorMessage
            )

            DescriptionInputField(
                value = states.descriptionField.fieldValue,
                onValueChange = { viewModel.onDescriptionFieldTextChange(it) },
                isError = states.descriptionField.isError,
                errorMessage = states.descriptionField.errorMessage
            )

            if (states.labelField.labels.isNotEmpty()) {
                Labels(
                    modifier = Modifier.fillMaxWidth(),
                    labels = states.labelField.labels,
                    showCrossIcon = true,
                    crossIconOnClick = { label -> viewModel.onLabelRemove(label) }
                )
            }

            LabelInputField(
                value = states.labelField.fieldValue,
                onValueChange = { viewModel.onLabelFieldTextChange(it) },
                onAddLabel = { viewModel.onLabelAdd(it) },
                isError = states.labelField.isError,
                errorMessage = states.labelField.errorMessage
            )

            val buttonGroupItems = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss
                ),
                ButtonGroupItem(
                    text = stringResource(R.string.add),
                    onClick = { viewModel.addCommand(onSuccess = onDismiss) }
                )
            )

            OverflowButtonGroup(
                items = buttonGroupItems,
                modifier = Modifier.padding(top = Dimens.paddingSmall)
            )
        }
    }
}
