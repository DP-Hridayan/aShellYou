package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * A 6-digit OTP-style input field for entering pairing codes.
 * Each digit gets its own box.
 */
@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    digitCount: Int = 6
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val filtered = newValue.text.filter { it.isDigit() }.take(digitCount)
            textFieldValue = TextFieldValue(filtered, TextRange(filtered.length))
            onValueChange(filtered)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(digitCount) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    val isFocused = value.length == index

                    OtpDigitBox(
                        digit = char,
                        isFocused = isFocused
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean
) {
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else if (digit.isNotEmpty()) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
