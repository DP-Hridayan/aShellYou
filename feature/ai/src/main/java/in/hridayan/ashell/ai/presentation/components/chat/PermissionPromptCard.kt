package `in`.hridayan.ashell.ai.presentation.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.presentation.model.PermissionPrompt
import `in`.hridayan.ashell.core.resources.R

@Composable
fun PermissionPromptCard(
    modifier: Modifier = Modifier,
    prompt: PermissionPrompt
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.ai_chat_allow_run_command),
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = prompt.command,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TextButton(onClick = prompt.onDeny) {
                Text(stringResource(R.string.ai_chat_deny))
            }

            TextButton(onClick = prompt.onAllow) {
                Text(stringResource(R.string.ai_chat_allow))
            }

            Button(onClick = prompt.onAlwaysAllowExact) {
                Text(stringResource(R.string.always_allow_exact))
            }

            if (prompt.baseCommand.isNotBlank()) {
                Button(onClick = prompt.onAlwaysAllowBase) {
                    Text(stringResource(R.string.always_allow_base) + " (${prompt.baseCommand})")
                }
            }
        }
    }
}
