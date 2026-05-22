@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.ai.presentation.ui.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion

/**
 * Main analysis content view shown when a command is successfully analyzed.
 * Displays sections: Description → Danger → Warnings → Root/Reversible → Use Cases → Examples → Corrections
 */
@OptIn(ExperimentalLayoutApi::class)
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
        // ── Description ──
        SectionCard(title = "Description") {
            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Danger Level ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DangerLevelIndicator(dangerLevel = result.dangerLevel)
        }

        Spacer(Modifier.height(8.dp))

        // ── Warnings ──
        if (result.warnings.isNotEmpty()) {
            SectionCard(title = "⚠️ Warnings") {
                result.warnings.forEach { warning ->
                    Text(
                        text = "• $warning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Root & Reversible badges ──
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (result.requiresRoot) {
                AssistChip(
                    onClick = {},
                    label = { Text("Requires Root") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(if (result.reversible) "Reversible" else "Irreversible")
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Use Cases ──
        if (result.useCases.isNotEmpty()) {
            SectionCard(title = "Use Cases") {
                result.useCases.forEach { useCase ->
                    Text(
                        text = "• $useCase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Examples ──
        if (result.examples.isNotEmpty()) {
            SectionCard(title = "Example Commands") {
                result.examples.forEach { example ->
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Corrections ──
        if (result.corrections.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            CorrectionSection(
                corrections = result.corrections,
                onApplyCorrection = onApplyCorrection
            )
        }

        // ── Feedback ──
        if (result.feedback.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = result.feedback,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
