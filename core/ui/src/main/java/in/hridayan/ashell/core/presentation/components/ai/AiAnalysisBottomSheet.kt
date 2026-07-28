@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisStatus
import `in`.hridayan.ashell.core.common.domain.model.ai.CorrectionSuggestion
import `in`.hridayan.ashell.core.presentation.model.AiAnalysisUiState
import `in`.hridayan.ashell.core.resources.R

/**
 * Material 3 modal bottom sheet for AI command analysis results.
 *
 * Supports partial ? expanded states and displays different content
 * based on the current [AiAnalysisUiState].
 */
@Composable
fun AiAnalysisBottomSheet(
    modifier: Modifier = Modifier,
    uiState: AiAnalysisUiState,
    onDismiss: () -> Unit,
    onApplyCorrection: (CorrectionSuggestion) -> Unit,
    onTryExample: () -> Unit,
    onRetry: () -> Unit,
) {
    val sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = null
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(R.string.command_analysis),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        when (uiState) {
            is AiAnalysisUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is AiAnalysisUiState.Success -> {
                val result = uiState.result
                when (result.status) {
                    AnalysisStatus.GIBBERISH -> {
                        GibberishContent(
                            feedback = result.feedback,
                            onTryExample = onTryExample
                        )
                    }

                    else -> {
                        AnalysisContent(
                            result = result,
                            onApplyCorrection = onApplyCorrection
                        )
                    }
                }
            }

            is AiAnalysisUiState.Error -> {
                ErrorContent(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }


            is AiAnalysisUiState.Idle -> {
                // Should not be visible when Idle
            }
        }

        // Bottom spacing for gesture navigation
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "??",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Analysis Failed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}
