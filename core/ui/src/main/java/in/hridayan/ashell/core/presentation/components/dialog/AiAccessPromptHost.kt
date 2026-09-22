package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ActiveProviderKeyStatus
import `in`.hridayan.ashell.core.presentation.viewmodel.ActiveProviderViewModel

/**
 * Shows the right prompt when an AI feature cannot run, for any screen that offers one.
 *
 * Which prompt appears depends on why the feature is blocked. With nothing selected the user needs
 * to add a first key; with a provider selected but no key for it, switching providers is usually
 * the faster fix, so the picker is offered inline.
 *
 * @param status Why the feature is blocked, or `null` when nothing should be shown.
 * @param onAddApiKey Invoked when the user chooses to go and add a key.
 */
@Composable
fun AiAccessPromptHost(
    status: ActiveProviderKeyStatus?,
    onDismiss: () -> Unit,
    onAddApiKey: () -> Unit,
    viewModel: ActiveProviderViewModel = hiltViewModel(),
) {
    var isPickingProvider by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        if (status == null) isPickingProvider = false
    }

    if (isPickingProvider) {
        val dialogState by viewModel.dialogState.collectAsState()

        ActiveProviderDialog(
            providers = viewModel.providers,
            selected = dialogState.selected,
            errorMessage = dialogState.error,
            onSelect = viewModel::select,
            onConfirm = {
                viewModel.confirm {
                    isPickingProvider = false
                    onDismiss()
                }
            },
            onDismiss = {
                isPickingProvider = false
                onDismiss()
            },
        )
        return
    }

    when (status) {
        is ActiveProviderKeyStatus.NoKeyFor -> NoKeyForActiveProviderDialog(
            provider = status.provider,
            onDismiss = onDismiss,
            onChooseProvider = {
                viewModel.prepare()
                isPickingProvider = true
            },
        )

        is ActiveProviderKeyStatus.NoProviderSelected -> ApiKeyRequiredDialog(
            onDismiss = onDismiss,
            onConfirm = onAddApiKey,
        )

        else -> Unit
    }
}
