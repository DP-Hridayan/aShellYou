@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.card

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard

@Composable
fun CommandSuggestionsCard(
    modifier: Modifier = Modifier,
    command: String,
    roundedCornerShape: RoundedCornerShape
) {
    RoundedCornerCard(
        modifier = modifier,
        roundedCornerShape = roundedCornerShape
    ) {
        Text(
            text = command,
            style = MaterialTheme.typography.titleMediumEmphasized,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}