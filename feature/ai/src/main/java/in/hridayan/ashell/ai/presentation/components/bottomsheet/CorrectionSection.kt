package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.domain.model.CorrectionConfidence
import `in`.hridayan.ashell.ai.domain.model.CorrectionSource
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion

/**
 * Section displaying correction suggestions for PARTIAL/INVALID commands.
 * Each suggestion shows the corrected command, confidence, source, and an apply button.
 */
@Composable
fun CorrectionSection(
    corrections: List<CorrectionSuggestion>,
    onApplyCorrection: (CorrectionSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    if (corrections.isEmpty()) return

    SectionCard(title = "Suggestions", modifier = modifier) {
        corrections.forEachIndexed { index, correction ->
            CorrectionCard(
                correction = correction,
                onApply = { onApplyCorrection(correction) }
            )
            if (index < corrections.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CorrectionCard(
    correction: CorrectionSuggestion,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Command text in monospace
            Text(
                text = correction.suggestedCommand,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confidence + Source badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConfidenceBadge(correction.confidence)
                    SourceBadge(correction.source)
                }

                // Apply button
                FilledTonalButton(
                    onClick = onApply,
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Apply", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: CorrectionConfidence) {
    val (color, text) = when (confidence) {
        CorrectionConfidence.HIGH -> MaterialTheme.colorScheme.tertiary to "High"
        CorrectionConfidence.MEDIUM -> MaterialTheme.colorScheme.secondary to "Medium"
        CorrectionConfidence.LOW -> MaterialTheme.colorScheme.outline to "Low"
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SourceBadge(source: CorrectionSource) {
    val text = when (source) {
        CorrectionSource.DATABASE -> "Database"
        CorrectionSource.HEURISTIC -> "Heuristic"
        CorrectionSource.AI -> "AI"
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
