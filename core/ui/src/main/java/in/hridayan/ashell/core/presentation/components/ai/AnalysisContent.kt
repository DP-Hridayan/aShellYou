@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.ai

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionConfidence
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionSource
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionSuggestion
import `in`.hridayan.ashell.core.common.domain.model.ai.DangerLevel
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils

/**
 * Main analysis content view shown when a command is successfully analyzed.
 * Displays the command description, danger level, use cases, and correction suggestions.
 */
@Composable
fun AnalysisContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    result: AnalysisResult,
    onApplyCorrection: (CorrectionSuggestion) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape((24.dp)))
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (result.command.isNotBlank()) {
            CommandHeaderCard(
                modifier = Modifier.fillMaxWidth(),
                command = result.command,
                dangerLevel = result.dangerLevel,
                onCopyCommand = {
                    ClipboardUtils.copyToClipboard(
                        text = result.command,
                        context = context
                    )
                }
            )
        }

        SectionCard(title = stringResource(R.string.description)) {
            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (result.useCases.isNotEmpty()) {
            SectionCard(title = stringResource(R.string.use_cases)) {
                result.useCases.forEach { useCase ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alignBy { it.measuredHeight }
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = useCase,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.alignBy(FirstBaseline)
                        )
                    }
                }
            }
        }

        if (result.feedback.isNotBlank()) {
            SectionCard(title = stringResource(R.string.feedback)) {
                Text(
                    text = result.feedback,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CommandHeaderCard(
    modifier: Modifier = Modifier,
    command: String,
    dangerLevel: DangerLevel,
    onCopyCommand: () -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 5.dp),
                text = stringResource(R.string.command),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            DangerLevelIndicator(dangerLevel = dangerLevel)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = command,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = withHaptic { onCopyCommand() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = null
                )
            }
        }
    }
}
