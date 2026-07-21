package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Humorous/friendly state displayed when the command is detected as GIBBERISH.
 * Avoids technical overload and instead provides a lighthearted response.
 */
@Composable
fun GibberishContent(
    feedback: String,
    onTryExample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val funMessages = remember {
        listOf(
            "🤔 That's not a command we've seen before!",
            "🧐 Hmm, this doesn't look like any shell command we know.",
            "😅 Even the AI is scratching its head on this one!",
            "🪄 That might work in a parallel universe, but not here.",
            "🤷 We searched high and low, but couldn't find this command.",
            "🎲 That's more creative writing than command-line!",
            "🌀 The terminal would be very confused by this.",
        )
    }

    val randomMessage = remember { funMessages.random() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🤖",
            fontSize = 56.sp,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = randomMessage,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (feedback.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onTryExample) {
            Text("Try an example command")
        }
    }
}
