package `in`.hridayan.ashell.presentation.ui.component.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LabelText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )
}
