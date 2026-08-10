package `in`.hridayan.ashell.ai.presentation.components.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.presentation.components.animatedcomposable.AnimatedStopIcon
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.resources.R

@Composable
fun PromptInputField(
    modifier: Modifier = Modifier,
    value: TextFieldValue = TextFieldValue(""),
    onValueChange: (TextFieldValue) -> Unit = {},
    isGenerating: Boolean,
    onClickTrailingButton: () -> Unit
) {
    CustomSearchBar(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        hint = stringResource(R.string.message_adb_agent),
        singleLine = false,
        leadingIcon = {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_help),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = onClickTrailingButton
            ) {
                if (isGenerating) {
                    AnimatedStopIcon(tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}
