@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.aimodels.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.data.local.model.ModelRegistry
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.page.aimodels.components.CacheDurationDialog
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent

@Composable
fun AiModelsScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: AiModelManagerViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val controller = settingsViewModel.rememberController()
    val settings = LocalSettings.current

    val selectedModelName = ModelRegistry.findById(settings.selectedModelId)?.name
    val cacheDays = settings.aiCacheDays

    val cacheSizeBytes by aiViewModel.cacheSizeBytes.collectAsState()

    LaunchedEffect(Unit) {
        aiViewModel.refreshCacheSize()
    }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.Navigate -> navController.navigate(event.route)
                is SettingsUiEvent.ShowDialog -> dialogManager.show(event.key)
                else -> {}
            }
        }
    }

    // Cache duration dialog
    DialogKey.Settings.AiCacheDays.createDialog { dm ->
        CacheDurationDialog(
            currentDays = cacheDays,
            onDismiss = { dm.dismiss() },
            onConfirm = { days ->
                settingsViewModel.setInt(SettingsKeys.AI_CACHE_DAYS, days)
                dm.dismiss()
            }
        )
    }

    val listState = rememberLazyListState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.aiModelsPage,
        listState = listState,
        headerItemCount = 0,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
    )

    val page = remember { settingsViewModel.aiModelsPage }

    val resolvedGroups = page.resolveAll(
        highlightedKey = highlightedKey,
        descriptionOverrides = mapOf(
            SettingsKeys.AI_MODELS to {
                selectedModelName ?: stringResource(R.string.no_model_selected)
            },
            SettingsKeys.AI_CACHE_DAYS to {
                stringResource(R.string.n_days, cacheDays)
            },
        ),
    )

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.ai_models),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                settingsContent(
                    groups = resolvedGroups,
                    controller = controller,
                )

                // Cache info card
                item {
                    CacheInfoCard(
                        cacheSizeBytes = cacheSizeBytes,
                        onClearCache = {
                            aiViewModel.clearCache()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )
}

@Composable
private fun CacheInfoCard(
    cacheSizeBytes: Long,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedSize = remember(cacheSizeBytes) {
        Formatter.formatShortFileSize(context, cacheSizeBytes)
    }

    CustomCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.clear_analysis_cache),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Text(
                    text = stringResource(R.string.cache_size, formattedSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onClearCache) {
                Icon(
                    Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(text = stringResource(R.string.clear))
            }
        }
    }
}
