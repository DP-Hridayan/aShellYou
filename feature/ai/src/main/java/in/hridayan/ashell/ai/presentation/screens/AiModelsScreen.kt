@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.Memory
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
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
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
import `in`.hridayan.settingsdsl.dsl.category
import `in`.hridayan.settingsdsl.dsl.clickableItem
import `in`.hridayan.settingsdsl.dsl.settingsPage
import `in`.hridayan.settingsdsl.dsl.switchItem
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent

enum class AiDialogKey : DialogKey {
    CacheDays,
    CacheClearConfirmation
}

private val aiModelsPage = settingsPage(
    screenTitle = R.string.ai_models, // can reuse string or change to AI Settings later
    screenId = "ai_settings",
    category(
        titleResId = R.string.models,
        clickableItem(
            key = SettingsKeys.CloudModels,
            titleResId = R.string.models,
            descriptionResId = R.string.des_models,
            iconVector = Icons.Rounded.Memory
        ),
    ),
    category(
        titleResId = R.string.cache_settings,
        switchItem(
            key = SettingsKeys.AiCacheEnabled,
            titleResId = R.string.ai_cache_enabled,
            descriptionResId = R.string.des_ai_cache_enabled,
            iconVector = Icons.Rounded.Cached
        ),
        clickableItem(
            key = SettingsKeys.AiCacheDays,
            titleResId = R.string.ai_cache_days,
            descriptionResId = R.string.des_ai_cache_days,
            icon = R.drawable.ic_schedule
        ),
        clickableItem(
            key = SettingsKeys.AiCacheClear,
            titleResId = R.string.clear_analysis_cache,
            descriptionResId = R.string.cache_size,
            icon = R.drawable.ic_delete_sweep
        )
    ),
    category(
        titleResId = R.string.agent_skills,
        switchItem(
            key = SettingsKeys.AiSkillCommandExecution,
            titleResId = R.string.command_execution,
            descriptionResId = R.string.des_command_execution,
            icon = R.drawable.ic_terminal
        ),
        switchItem(
            key = SettingsKeys.AiSkillQuickSettings,
            titleResId = R.string.quick_settings_tiles,
            descriptionResId = R.string.des_quick_settings_tiles,
            icon = R.drawable.ic_dashboard
        ),
        switchItem(
            key = SettingsKeys.AiSkillPackages,
            titleResId = R.string.packages,
            descriptionResId = R.string.des_packages,
            icon = R.drawable.ic_package
        ),
        switchItem(
            key = SettingsKeys.AiSkillDatabase,
            titleResId = R.string.database_modification,
            descriptionResId = R.string.des_database_modification,
            icon = R.drawable.ic_database
        )
    )
)

@Composable
fun AiModelsScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
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
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = aiModelsPage,
        listState = listState,
        headerItemCount = 0,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
        topAppBarState = topAppBarState,
    )

    val resolvedGroups = aiModelsPage.resolveAll(
        highlightedKey = highlightedKey,
        descriptionOverrides = mapOf(

            SettingsKeys.AiCacheDays to {
                stringResource(R.string.n_days, cacheDays)
            },
            SettingsKeys.AiCacheClear to {
                stringResource(R.string.cache_size, formattedSize)
            }
        ),
    )

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
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
                    hapticsEnabled = hapticsEnabled,
                    isChecked = { key ->
                        val sk = key as? SettingsKeys<*> ?: return@settingsContent false
                        if (sk.default !is Boolean) return@settingsContent false
                        prefs[booleanPreferencesKey(sk.name)] ?: (sk.default as Boolean)
                    },
                    selectedValue = { key ->
                        val sk = key as? SettingsKeys<*> ?: return@settingsContent -1
                        if (sk.default !is Int) return@settingsContent -1
                        prefs[intPreferencesKey(sk.name)] ?: (sk.default as Int)
                    },
                    onItemClick = { key ->
                        when (key) {
                            SettingsKeys.AiCloudProvider -> navController.navigate(NavRoutes.CloudModelsScreen)
                            SettingsKeys.AiCacheDays -> dialogManager.show(AiDialogKey.CacheDays)
                            SettingsKeys.AiCacheClear -> dialogManager.show(AiDialogKey.CacheClearConfirmation)
                        }
                    },
                    onBooleanToggle = { key ->
                        @Suppress("UNCHECKED_CAST")
                        val typedKey = key as? SettingsKeys<Boolean> ?: return@settingsContent
                        aiViewModel.toggleSetting(typedKey)
                    },

                    onIntChanged = { key, value ->
                        @Suppress("UNCHECKED_CAST")
                        val typedKey = key as? SettingsKeys<Int> ?: return@settingsContent
                        aiViewModel.setInt(typedKey, value)
                    }
                )

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
