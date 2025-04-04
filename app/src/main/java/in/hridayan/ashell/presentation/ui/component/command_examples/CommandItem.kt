package `in`.hridayan.ashell.presentation.ui.component.command_examples

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.presentation.ui.theme.Dimens

@Composable
fun CommandItem(modifier: Modifier = Modifier, text: String) {
    Card(modifier = modifier.fillMaxWidth().padding(Dimens.paddingExtraSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(modifier = Modifier,
            text = text )
    }
}