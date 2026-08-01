package `in`.hridayan.ashell.ai.presentation.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

@Composable
fun NewChatPromptSuggestions(
    modifier: Modifier = Modifier,
    onClickPrompt: (String) -> Unit
) {
    val prompts = listOf(
        stringResource(R.string.ai_prompt_suggestion_1),
        stringResource(R.string.ai_prompt_suggestion_2),
        stringResource(R.string.ai_prompt_suggestion_3)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        prompts.forEach { prompt ->
            OutlinedButton(
                modifier = Modifier,
                shape = ButtonDefaults.shape,
                onClick = withHaptic { onClickPrompt(prompt) },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    text = prompt,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
