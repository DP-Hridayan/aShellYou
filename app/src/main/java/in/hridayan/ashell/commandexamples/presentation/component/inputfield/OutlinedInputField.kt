package `in`.hridayan.ashell.commandexamples.presentation.component.inputfield

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OutlinedInputField(hint: String, errorHint : String = "", value: String, onValueChange: (String) -> Unit, isError: Boolean, modifier: Modifier) {
    val label =
        if (isError) errorHint else hint

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError
    )
}