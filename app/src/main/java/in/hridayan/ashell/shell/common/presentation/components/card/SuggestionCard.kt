@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.common.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.shell.common.domain.model.Suggestion
import `in`.hridayan.ashell.shell.common.domain.model.SuggestionType
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.ShellViewModel

@Composable
fun SuggestionCard(
    modifier: Modifier = Modifier,
    suggestion: Suggestion,
    roundedCornerShape: RoundedCornerShape,
    viewModel: ShellViewModel = hiltViewModel()
) {
    val containerColor = when (suggestion.type) {
        SuggestionType.COMMAND -> MaterialTheme.colorScheme.surfaceContainer
        SuggestionType.PACKAGE -> MaterialTheme.colorScheme.primaryContainer
        SuggestionType.PERMISSION -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (suggestion.type) {
        SuggestionType.COMMAND -> MaterialTheme.colorScheme.onSurface
        SuggestionType.PACKAGE -> MaterialTheme.colorScheme.onPrimaryContainer
        SuggestionType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer
    }

    RoundedCornerCard(
        modifier = modifier,
        onClick = withHaptic { viewModel.applySuggestion(suggestion) },
        roundedCornerShape = roundedCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = contentColor,
                modifier = Modifier.weight(1f, fill = false)
            )

            suggestion.label?.let { label ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (label == "System")
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (label == "System")
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

