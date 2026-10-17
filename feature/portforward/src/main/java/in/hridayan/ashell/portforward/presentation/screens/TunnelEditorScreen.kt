@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.portforward.presentation.screens

import android.content.Context
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.LocalSnackBarController
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarController
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.ToastUtils
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.browserUrl
import `in`.hridayan.ashell.portforward.presentation.components.editor.EditorActionBar
import `in`.hridayan.ashell.portforward.presentation.components.editor.TunnelEditorContent
import `in`.hridayan.ashell.portforward.presentation.components.editor.labelRes
import `in`.hridayan.ashell.portforward.presentation.components.editor.needsLocalNetworkPermission
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorEvent
import `in`.hridayan.ashell.portforward.presentation.permission.LocalNetworkAccessState
import `in`.hridayan.ashell.portforward.presentation.permission.rememberLocalNetworkAccess
import `in`.hridayan.ashell.portforward.presentation.permission.rememberNotificationPermissionRequest
import `in`.hridayan.ashell.portforward.presentation.viewmodel.TunnelEditorViewModel

/** Creates a tunnel, or edits one when opened with a tunnel id. */
@Composable
fun TunnelEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: TunnelEditorViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localNetwork = rememberLocalNetworkAccess()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val startWithNotificationPrompt = rememberNotificationPermissionRequest { viewModel.save(start = true) }

    val snackBar = LocalSnackBarController.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event -> handleEvent(event, navController, context, snackBar) }
    }

    val actions = editorActions(
        viewModel = viewModel,
        context = context,
        localNetwork = localNetwork,
        onStart = startWithNotificationPrompt,
        onCancel = { navController.navigateBack() }
    )

    AppScaffold(
        modifier = modifier,
        topBarTitle = stringResource(if (state.isEditing) R.string.port_forward_edit_tunnel else R.string.new_tunnel),
        listState = listState,
        topAppBarState = topAppBarState,
        onNavigateBack = { navController.navigateBack() },
        fabContent = { _ ->
            EditorActionBar(
                isRunning = state.isRunning,
                enabled = !state.isSaving && !state.needsLocalNetworkPermission(localNetwork),
                onSave = actions.onSave,
                onStart = actions.onStart
            )
        },
        content = { innerPadding, topBarScrollBehavior ->
            TunnelEditorContent(
                state = state,
                localNetwork = localNetwork,
                actions = actions,
                listState = listState,
                contentPadding = innerPadding,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            )
        }
    )
}

private fun editorActions(
    viewModel: TunnelEditorViewModel,
    context: Context,
    localNetwork: LocalNetworkAccessState,
    onStart: () -> Unit,
    onCancel: () -> Unit
) = TunnelEditorActions(
    onAddressChange = viewModel::onAddressChange,
    onPaste = { ClipboardUtils.readFromClipboard(context)?.let(viewModel::onAddressChange) },
    onHostChange = viewModel::onHostChange,
    onTargetPortChange = viewModel::onTargetPortChange,
    onEndpointTypeChange = viewModel::onEndpointTypeChange,
    onEndpointValueChange = viewModel::onEndpointValueChange,
    onPreset = { preset -> viewModel.onPresetSelected(preset, context.getString(preset.labelRes())) },
    onLocalPortChange = viewModel::onLocalPortChange,
    onSameAsTargetChange = viewModel::onSameAsTargetChange,
    onNameChange = viewModel::onNameChange,
    onAutoStartChange = viewModel::onAutoStartChange,
    onShareOnNetworkChange = viewModel::onShareOnNetworkChange,
    onRecentHost = viewModel::onRecentHostSelected,
    onUseSuggestedPort = viewModel::useSuggestedPort,
    onRequestLocalNetwork = localNetwork.request,
    onOpenPermissionSettings = localNetwork.openSettings,
    onTest = viewModel::test,
    onSave = { viewModel.save(start = false) },
    onStart = onStart,
    onCancel = onCancel
)

private fun handleEvent(
    event: TunnelEditorEvent,
    navController: NavController,
    context: Context,
    snackBar: SnackBarController
) {
    when (event) {
        is TunnelEditorEvent.Saved -> {
            if (event.started) showRunningSnackBar(event.spec, context, snackBar)
            navController.navigateBack()
        }

        TunnelEditorEvent.SaveFailed ->
            ToastUtils.makeToast(context, context.getString(R.string.port_forward_save_failed))
    }
}

private fun showRunningSnackBar(spec: TunnelSpec, context: Context, snackBar: SnackBarController) {
    val message = context.getString(R.string.port_forward_tunnel_running, spec.name)
    if (!spec.looksLikeHttp) {
        snackBar.show(message)
        return
    }
    snackBar.show(
        message = message,
        actionText = context.getString(R.string.open),
        onActionClicked = { UrlUtils.openUrl(spec.browserUrl, context) }
    )
}
