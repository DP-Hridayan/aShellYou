@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionConfidence
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionSource
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionSuggestion

/**
 * Main analysis content view shown when a command is successfully analyzed.
 * Displays the command description, danger level, use cases, and correction suggestions.
 */
@Composable
fun AnalysisContent(
    result: AnalysisResult,
    onApplyCorrection: (CorrectionSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        if (result.command.isNotBlank()) {
            Text(
                text = result.command,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            DangerLevelIndicator(dangerLevel = result.dangerLevel)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Description ──
        SectionCard(title = "Description") {
            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Use Cases ──
        if (result.useCases.isNotEmpty()) {
            SectionCard(title = "Use Cases") {
                result.useCases.forEach { useCase ->
                    Text(
                        text = "• $useCase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        // ── Feedback ──
        if (result.feedback.isNotBlank()) {
            SectionCard(title = "Feedback") {
                Text(
                    text = result.feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // ── Suggestion ──
        if (!result.suggestedCorrection.isNullOrBlank()) {
            val suggestion = CorrectionSuggestion(
                suggestedCommand = result.suggestedCorrection!!,
                confidence = CorrectionConfidence.HIGH,
                source = CorrectionSource.AI
            )
            CorrectionSection(
                corrections = listOf(suggestion),
                onApplyCorrection = onApplyCorrection
            )
        }
    }
}
