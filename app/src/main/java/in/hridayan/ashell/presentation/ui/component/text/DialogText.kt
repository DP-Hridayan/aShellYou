package `in`.hridayan.ashell.presentation.ui.component.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DialogTitle(text : String,modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DialogSubTitle(text : String,modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DialogDescription(text: String,modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}