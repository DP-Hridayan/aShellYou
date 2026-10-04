package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.FilterMode

/** Whether a profile shows or hides what it matches, in tertiary for Include and error for Exclude. */
@Composable
fun FilterModeBadge(mode: FilterMode, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content, label) = when (mode) {
        FilterMode.INCLUDE -> Triple(colors.tertiary, colors.onTertiary, R.string.include)
        FilterMode.EXCLUDE -> Triple(colors.error, colors.onError, R.string.exclude)
    }

    Badge(modifier = modifier, containerColor = container, contentColor = content) {
        Text(text = stringResource(label), style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
