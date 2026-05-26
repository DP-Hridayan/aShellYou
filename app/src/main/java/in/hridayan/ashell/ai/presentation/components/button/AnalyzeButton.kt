package `in`.hridayan.ashell.ai.presentation.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * "Analyze with AI" icon button for the shell input area.
 * Uses the AutoAwesome (sparkle/AI) icon to indicate AI-powered functionality.
 */
@Composable
fun AnalyzeButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = "Analyze with AI",
            modifier = Modifier.size(22.dp)
        )
    }
}
