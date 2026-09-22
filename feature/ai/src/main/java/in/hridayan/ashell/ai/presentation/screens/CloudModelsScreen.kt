@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.ai.presentation.components.card.ApiKeyCard
import `in`.hridayan.ashell.ai.presentation.viewmodel.CloudModelsViewModel
import `in`.hridayan.ashell.ai.presentation.viewmodel.VerificationState
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.ActiveProviderDialog
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.viewmodel.ActiveProviderViewModel
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsgraph.ui.SettingsColumn

private const val ITEM_KEY_API_KEY_PREFIX = "api_key_"
private const val ITEM_KEY_API_KEY_HEADER = "api_key_header"

private val HEADER_HORIZONTAL_PADDING = 20.dp
private val HEADER_TOP_PADDING = 30.dp
private val HEADER_BOTTOM_PADDING = 10.dp

@Composable
fun CloudModelsScreen(
    viewModel: CloudModelsViewModel = hiltViewModel(),
    activeProviderViewModel: ActiveProviderViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val dialogManager = LocalDialogManager.current
    val settings = LocalSettings.current

    val verificationStates by viewModel.verificationStates.collectAsState()
    val activeProvider by activeProviderViewModel.activeProvider.collectAsState()

    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val activeProviderLabel = activeProvider?.displayName ?: stringResource(R.string.none)

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBarTitle = stringResource(R.string.cloud_models),
        listState = listState,
        topAppBarState = topAppBarState,
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, topBarScrollBehavior ->
            SettingsColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                contentPadding = innerPadding,
                topAppBarState = topAppBarState,
                hapticsEnabled = hapticsEnabled,
            ) {
                group(R.string.active_provider) {
                    clickableItem(SettingsKeys.AiCloudProvider) {
                        title(R.string.active_provider)
                        description(activeProviderLabel)
                        icon(R.drawable.ic_cloud_model)
                        onClick {
                            activeProviderViewModel.prepare()
                            dialogManager.show(AiDialogKey.ActiveProvider)
                        }
                    }
                }

                item(ITEM_KEY_API_KEY_HEADER) {
                    ApiKeyGroupHeader()
                }

                LlmProvider.all.forEach { provider ->
                    item(ITEM_KEY_API_KEY_PREFIX + provider.id) {
                        val hasKey by viewModel.hasKey(provider).collectAsState(initial = false)

                        ApiKeyCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 5.dp),
                            provider = provider,
                            hasKey = hasKey,
                            verificationState = verificationStates[provider.id]
                                ?: VerificationState.Idle,
                            onSaveApiKey = { key -> viewModel.saveApiKey(provider, key) },
                            onDeleteApiKey = { viewModel.deleteApiKey(provider) },
                            onVerifyApiKey = { viewModel.verifyKey(provider) },
                        )
                    }
                }
            }
        }
    )

    AiDialogKey.ActiveProvider.createDialog { manager ->
        val dialogState by activeProviderViewModel.dialogState.collectAsState()

        ActiveProviderDialog(
            providers = activeProviderViewModel.providers,
            selected = dialogState.selected,
            errorMessage = dialogState.error,
            onSelect = activeProviderViewModel::select,
            onConfirm = { activeProviderViewModel.confirm(onSaved = manager::dismiss) },
            onDismiss = manager::dismiss,
        )
    }
}

/**
 * Stands in for a settings group header.
 *
 * The API key cards are plain items rather than settings nodes, and the DSL drops a group that
 * holds no nodes, so the heading is drawn here using the same metrics the library uses.
 */
@Composable
private fun ApiKeyGroupHeader() {
    Text(
        text = stringResource(R.string.api_key),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = HEADER_HORIZONTAL_PADDING,
            end = HEADER_HORIZONTAL_PADDING,
            top = HEADER_TOP_PADDING,
            bottom = HEADER_BOTTOM_PADDING,
        )
    )
}
