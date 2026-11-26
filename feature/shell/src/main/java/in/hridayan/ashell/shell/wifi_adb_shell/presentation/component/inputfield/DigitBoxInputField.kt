package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.inputfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * A 6-digit OTP-style input field for entering pairing codes.
 * Each digit gets its own box.
 */
@Composable
fun DigitBoxInputField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    digitCount: Int = 6
) {
    BasicTextField(
        state = state,
        inputTransformation = InputTransformation {
            val filtered = asCharSequence().filter { it.isDigit() }.take(digitCount)
            if (filtered.length != length || !asCharSequence().contentEquals(filtered)) {
                replace(0, length, filtered)
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorator = TextFieldDecorator {
            val currentText = state.text
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(digitCount) { index ->
                    val char = currentText.getOrNull(index)?.toString() ?: ""
                    val isFocused = currentText.length == index

                    DigitBox(
                        digit = char,
                        isFocused = isFocused
                    )
                }
            }
        }
    )
}

@Composable
private fun DigitBox(
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
