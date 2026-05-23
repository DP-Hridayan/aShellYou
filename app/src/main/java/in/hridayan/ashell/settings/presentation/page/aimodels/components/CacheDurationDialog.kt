package `in`.hridayan.ashell.settings.presentation.page.aimodels.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R

private const val MIN_CACHE_DAYS = 1
private const val MAX_CACHE_DAYS = 365

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CacheDurationDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentDays.toString()) }

    val parsedValue = text.toIntOrNull()
    val isValid = parsedValue != null && parsedValue in MIN_CACHE_DAYS..MAX_CACHE_DAYS
    val isError = text.isNotEmpty() && !isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ai_cache_days)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 7, 30).forEach { days ->
                        FilterChip(
                            selected = parsedValue == days,
                            onClick = { text = days.toString() },
                            label = { Text(stringResource(R.string.n_days, days)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        // Accept only digits
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            text = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.ai_cache_days)) },
                    supportingText = {
                        if (isError) {
                            Text("$MIN_CACHE_DAYS – $MAX_CACHE_DAYS days")
                        }
                    },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedValue?.let { onConfirm(it) } },
                enabled = isValid
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
