@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.domain.model.AnalysisStatus
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion
import `in`.hridayan.ashell.ai.presentation.model.AiAnalysisUiState

/**
 * Material 3 modal bottom sheet for AI command analysis results.
 *
 * Supports partial → expanded states and displays different content
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
    onDownloadModel: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    ),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(R.string.command_analysis),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        when (uiState) {
            is AiAnalysisUiState.Loading -> {
                SkeletonLoadingContent()
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

            is AiAnalysisUiState.ModelNotInstalled -> {
                ModelNotInstalledContent(
                    onDownloadModel = onDownloadModel,
                    onDismiss = onDismiss
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
            text = "😵",
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

@Composable
private fun ModelNotInstalledContent(
    onDownloadModel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🤖",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "AI Model Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Download an AI model to enable offline command analysis. The model runs entirely on your device — no data is sent anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        FilledTonalButton(onClick = onDownloadModel) {
            Text("Download Model")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDismiss) {
            Text("Not Now")
        }
    }
}
