package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R

/** A text field that takes several comma-separated values, such as tags or process IDs. */
@Composable
fun FilterValuesField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { Text(stringResource(R.string.comma_separated_values)) },
        trailingIcon = trailingIcon,
        singleLine = true,
    )
}
