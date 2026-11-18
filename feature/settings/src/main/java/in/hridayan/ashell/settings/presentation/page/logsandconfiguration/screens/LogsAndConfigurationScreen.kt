package `in`.hridayan.ashell.settings.presentation.page.logsandconfiguration.screens

import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.logsandconfiguration.components.ExportConfigurationBottomSheet
import `in`.hridayan.ashell.settings.presentation.page.logsandconfiguration.viewmodel.LogsAndConfigurationViewModel
import `in`.hridayan.settingsgraph.ui.SettingsColumn

private const val SHARE_MIME_TYPE = "text/plain"
private const val ITEM_KEY_TOP_SPACER = "top_spacer"
private const val ITEM_KEY_BOTTOM_SPACER = "bottom_spacer"

@Composable
fun LogsAndConfigurationScreen(
    modifier: Modifier = Modifier,
    viewModel: LogsAndConfigurationViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val res = LocalResources.current
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showExportSheet by viewModel.showExportSheet.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.shareUriEvent.collect { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = SHARE_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    res.getString(R.string.export_config_share_subject)
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, res.getString(R.string.share))
            context.startActivity(chooser)
        }
    }

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.logs_and_configuration),
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
                item(ITEM_KEY_TOP_SPACER) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                }

                group(R.string.logs) {
                    clickableItem(SettingsKeys.CrashHistory) {
                        title(R.string.crash_history)
                        description(R.string.des_crash_history)
                        icon(R.drawable.ic_bug)
                        onClick { navController.navigate(NavRoutes.CrashHistoryScreen) }
                    }
                }

                group(R.string.configuration) {
                    clickableItem(SettingsKeys.ExportConfiguration) {
                        title(R.string.export_configuration)
                        description(R.string.des_export_configuration)
                        icon(R.drawable.ic_upload_file)
                        onClick { viewModel.openExportSheet() }
                    }
                }

                item(ITEM_KEY_BOTTOM_SPACER) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )

    if (showExportSheet) {
        ExportConfigurationBottomSheet(
            configPreview = uiState.configPreview,
            isLoading = uiState.isLoading,
            isExporting = uiState.isExporting,
            onDismiss = { viewModel.dismissExportSheet() },
            onExport = { viewModel.exportConfig() },
        )
    }
}
