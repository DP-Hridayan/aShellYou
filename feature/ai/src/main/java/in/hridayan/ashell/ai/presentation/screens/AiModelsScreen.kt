@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.emptyPreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.ai.presentation.components.dialog.CacheDurationDialog
import `in`.hridayan.ashell.ai.presentation.components.dialog.DeleteAiAnalysisCacheDialog
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsdsl.ui.SettingsColumn

enum class AiDialogKey : DialogKey {
    CacheDays,
    CacheClearConfirmation
}

@Composable
fun AiModelsScreen(
    modifier: Modifier = Modifier,
    aiViewModel: AiModelManagerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val settings = LocalSettings.current
    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]

    val prefs by aiViewModel.preferences.collectAsState(initial = emptyPreferences())

    val cacheDays = settings[SettingsKeys.AiCacheDays]

    val cacheSizeBytes by aiViewModel.cacheSizeBytes.collectAsState()

    val formattedSize = remember(cacheSizeBytes) {
        Formatter.formatShortFileSize(context, cacheSizeBytes)
    }

    LaunchedEffect(Unit) {
        aiViewModel.refreshCacheSize()
    }

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.ai_models),
        content = { innerPadding, topBarScrollBehavior ->
            SettingsColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                contentPadding = innerPadding,
                topAppBarState = topAppBarState,
                hapticsEnabled = hapticsEnabled,
            ) {
                group(R.string.models) {
                    clickableItem(SettingsKeys.AiCloudProvider) {
                        title(R.string.cloud_models)
                        description(R.string.des_cloud_models)
                        icon(R.drawable.ic_cloud_model)
                        onClick { key ->
                            when (key) {
                                SettingsKeys.AiCloudProvider -> navController.navigate(NavRoutes.CloudModelsScreen)
                                else -> {}
                            }
                        }
                    }
                }

                group(R.string.agent_skills) {
                    switchItem(SettingsKeys.AiSkillCommandExecution) {
                        title(R.string.command_execution)
                        description(R.string.des_command_execution)
                        icon(R.drawable.ic_terminal)
                        onClick { key ->
                            @Suppress("UNCHECKED_CAST")
                            val typedKey = key as? SettingsKeys<Boolean> ?: return@onClick
                            aiViewModel.toggleSetting(typedKey)
                        }
                    }

                    switchItem(SettingsKeys.AiSkillQuickSettings) {
                        title(R.string.quick_settings_tiles)
                        description(R.string.des_quick_settings_tiles)
                        icon(R.drawable.ic_dashboard)
                        onClick { key ->
                            @Suppress("UNCHECKED_CAST")
                            val typedKey = key as? SettingsKeys<Boolean> ?: return@onClick
                            aiViewModel.toggleSetting(typedKey)
                        }
                    }

                    switchItem(SettingsKeys.AiSkillPackages) {
                        title(R.string.packages)
                        description(R.string.des_packages)
                        icon(R.drawable.ic_package)
                        onClick { key ->
                            @Suppress("UNCHECKED_CAST")
                            val typedKey = key as? SettingsKeys<Boolean> ?: return@onClick
                            aiViewModel.toggleSetting(typedKey)
                        }
                    }

                    switchItem(SettingsKeys.AiSkillDatabase) {
                        title(R.string.database_modification)
                        description(R.string.des_database_modification)
                        icon(R.drawable.ic_database)
                        onClick { key ->
                            @Suppress("UNCHECKED_CAST")
                            val typedKey = key as? SettingsKeys<Boolean> ?: return@onClick
                            aiViewModel.toggleSetting(typedKey)
                        }
                    }
                }

                group(R.string.cache_settings) {
                    switchItem(SettingsKeys.AiCacheEnabled) {
                        title(R.string.ai_cache_enabled)
                        description(R.string.des_ai_cache_enabled)
                        icon(Icons.Rounded.Cached)
                        onClick { key ->
                            @Suppress("UNCHECKED_CAST")
                            val typedKey = key as? SettingsKeys<Boolean> ?: return@onClick
                            aiViewModel.toggleSetting(typedKey)
                        }
                    }

                    clickableItem(SettingsKeys.AiCacheDays) {
                        title(R.string.ai_cache_days)
                        description { stringResource(R.string.n_days, cacheDays) }
                        icon(R.drawable.ic_schedule)
                        onClick { dialogManager.show(AiDialogKey.CacheDays) }
                    }

                    clickableItem(SettingsKeys.AiCacheClear) {
                        title(R.string.clear_analysis_cache)
                        description { stringResource(R.string.cache_size, formattedSize) }
                        icon(R.drawable.ic_delete_sweep)
                        onClick { dialogManager.show(AiDialogKey.CacheClearConfirmation) }
                    }
                }
            }
        },
    )

    // Cache duration dialog
    AiDialogKey.CacheDays.createDialog { dm ->
        CacheDurationDialog(
            currentDays = cacheDays,
            onDismiss = { dm.dismiss() },
            onConfirm = { days ->
                aiViewModel.setInt(SettingsKeys.AiCacheDays, days)
                dm.dismiss()
            }
        )
    }

    AiDialogKey.CacheClearConfirmation.createDialog { dm ->
        DeleteAiAnalysisCacheDialog(
            onDismiss = { dm.dismiss() },
            onDelete = {
                aiViewModel.clearCache()
                dm.dismiss()
            }
        )
    }
}

