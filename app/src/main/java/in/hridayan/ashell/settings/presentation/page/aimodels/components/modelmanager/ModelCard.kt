@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.aimodels.components.modelmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.presentation.model.DownloadProgress
import `in`.hridayan.ashell.ai.presentation.model.ModelCardState
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel.ModelUiState
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic

/**
 * Material 3 card displaying a single AI model's info and actions.
 * Supports states: NOT_INSTALLED, DOWNLOADING, INSTALLED, SELECTED, ERROR.
 */
@Composable
fun ModelCard(
    state: ModelUiState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modelDetails =
        listOf(state.model.parameterCount, state.model.quantization, state.model.formattedSize)

    val containerColor = MaterialTheme.colorScheme.run {
        if (state.cardState == ModelCardState.SELECTED) primaryContainer else surfaceContainer
    }
    val contentColor = MaterialTheme.colorScheme.run {
        if (state.cardState == ModelCardState.SELECTED) onPrimaryContainer else onSurface
    }

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: Name + badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.model.name,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                        if (state.model.isRecommended) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        stringResource(R.string.recommended),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        modelDetails.forEach { detail ->

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    text = detail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }
                }

                // Status icon
                when (state.cardState) {
                    ModelCardState.SELECTED -> Icon(
                        Icons.Rounded.Verified,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )

                    ModelCardState.ERROR -> Icon(
                        Icons.Rounded.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )

                    else -> {}
                }
            }

            Spacer(Modifier.height(10.dp))

            // Description
            Text(
                text = state.model.description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(12.dp))

            // Download progress
            AnimatedVisibility(visible = state.cardState == ModelCardState.DOWNLOADING) {
                val progress = state.downloadProgress
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (progress is DownloadProgress.Downloading) {
                        LinearWavyProgressIndicator(
                            progress = { progress.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${progress.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    } else {
                        LoadingIndicator(
                            modifier = Modifier
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Error message
            if (state.cardState == ModelCardState.ERROR && state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state.cardState) {
                    ModelCardState.NOT_INSTALLED -> {
                        FilledTonalButton(
                            onClick = withHaptic { onDownload() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                Icons.Rounded.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.download))
                        }
                    }

                    ModelCardState.DOWNLOADING -> {
                        OutlinedButton(
                            onClick = withHaptic { onCancelDownload() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                Icons.Rounded.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.cancel))
                        }
                    }

                    ModelCardState.INSTALLED -> {
                        OutlinedButton(
                            onClick = withHaptic { onDelete() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }

                        Spacer(Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = withHaptic { onSelect() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(stringResource(R.string.select))
                        }
                    }

                    ModelCardState.SELECTED -> {
                        OutlinedButton(
                            onClick = withHaptic { onDelete() },
                            shapes = ButtonDefaults.shapes(),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                        ) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }

                        Spacer(Modifier.width(8.dp))
                    }

                    ModelCardState.ERROR -> {
                        OutlinedButton(
                            onClick = withHaptic { onDismissError() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(stringResource(R.string.dismiss))
                        }

                        Spacer(Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = withHaptic { onDownload() },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}
