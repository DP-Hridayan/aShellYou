package `in`.hridayan.ashell.commandexamples.presentation.component.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

private const val MIN_INPUT_LINES = 2
private const val MAX_INPUT_LINES = 5

@Composable
fun CommandInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.command)) },
        isError = isError,
        supportingText = if (isError && errorMessage.isNotEmpty()) {
            { Text(errorMessage) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
        singleLine = false,
        minLines = MIN_INPUT_LINES,
        maxLines = MAX_INPUT_LINES,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DescriptionInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.description)) },
        isError = isError,
        supportingText = if (isError && errorMessage.isNotEmpty()) {
            { Text(errorMessage) }
        } else {
            null
        },
        singleLine = false,
        minLines = MIN_INPUT_LINES,
        maxLines = MAX_INPUT_LINES,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun LabelInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onAddLabel: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.label)) },
        isError = isError,
        supportingText = if (isError && errorMessage.isNotEmpty()) {
            { Text(errorMessage) }
        } else {
            null
        },
        trailingIcon = {
            IconButton(
                onClick = withHaptic {
                    onAddLabel(value.text)
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
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
