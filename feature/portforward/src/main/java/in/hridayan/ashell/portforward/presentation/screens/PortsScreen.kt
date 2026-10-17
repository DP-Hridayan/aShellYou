@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.portforward.presentation.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.common.LocalSnackBarController
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.snackbar.SnackBarController
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.core.utils.createAppNotificationSettingsIntent
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.browserUrl
import `in`.hridayan.ashell.portforward.domain.model.localAddress
import `in`.hridayan.ashell.portforward.presentation.components.PortsEmptyState
import `in`.hridayan.ashell.portforward.presentation.components.QuickForwardRow
import `in`.hridayan.ashell.portforward.presentation.components.ScopeChips
import `in`.hridayan.ashell.portforward.presentation.components.card.PortsBanner
import `in`.hridayan.ashell.portforward.presentation.components.card.TunnelCard
import `in`.hridayan.ashell.portforward.presentation.components.editor.EditorSectionLabel
import `in`.hridayan.ashell.portforward.presentation.components.editor.labelRes
import `in`.hridayan.ashell.portforward.presentation.components.sheet.KindPickerSheet
import `in`.hridayan.ashell.portforward.presentation.components.sheet.TunnelDetailSheet
import `in`.hridayan.ashell.portforward.presentation.model.BannerAction
import `in`.hridayan.ashell.portforward.presentation.model.PortsEvent
import `in`.hridayan.ashell.portforward.presentation.model.PortsScope
import `in`.hridayan.ashell.portforward.presentation.model.PortsUiState
import `in`.hridayan.ashell.portforward.presentation.model.TunnelCardActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelDetailActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelItem
import `in`.hridayan.ashell.portforward.presentation.permission.rememberNotificationPermissionRequest
import `in`.hridayan.ashell.portforward.presentation.permission.rememberNotificationsAllowed
import `in`.hridayan.ashell.portforward.presentation.viewmodel.PortsViewModel

private val HorizontalPadding = 16.dp

/** Lists saved tunnels, with their live state, and starts the editor for new ones. */
@Composable
fun PortsScreen(
    modifier: Modifier = Modifier,
    viewModel: PortsViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackBar = LocalSnackBarController.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showKindPicker by rememberSaveable { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<TunnelSpec?>(null) }
    val requestNotifications = rememberNotificationPermissionRequest {
        pendingStart?.let { viewModel.setRunning(it, true) }
        pendingStart = null
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event -> handleEvent(event, viewModel, context, snackBar) }
    }

    val cardActions: (TunnelItem) -> TunnelCardActions = { item ->
        TunnelCardActions(
            onClick = { selectedId = item.spec.id },
            onRunningChange = { running ->
                if (running) {
                    pendingStart = item.spec
                    requestNotifications()
                } else {
                    viewModel.setRunning(item.spec, false)
                }
            },
            onOpen = { UrlUtils.openUrl(item.spec.browserUrl, context) },
            onCopy = { ClipboardUtils.copyToClipboard(copyText(item.spec), context) },
            onUsePort = { port -> viewModel.moveToPort(item.spec, port) },
            onEdit = { navController.navigate(NavRoutes.TunnelEditorScreen(item.spec.id)) },
            onReconnect = { navController.navigate(NavRoutes.WifiAdbScreen()) },
            onUseDevice = { device -> viewModel.useConnectedDevice(item.spec, device) }
        )
    }

    val onNewTunnel: () -> Unit = {
        when (val scope = state.scope) {
            is PortsScope.Device -> navController.navigate(NavRoutes.TunnelEditorScreen(transport = scope.transport))
            PortsScope.Network -> navController.navigate(NavRoutes.TunnelEditorScreen())
            PortsScope.All -> if (state.connectedDevices.isEmpty()) {
                navController.navigate(NavRoutes.TunnelEditorScreen())
            } else {
                showKindPicker = true
            }
        }
    }

    AppScaffold(
        modifier = modifier,
        topBarTitle = stringResource(R.string.port_forwarding),
        listState = listState,
        topAppBarState = topAppBarState,
        onNavigateBack = { navController.navigateBack() },
        actions = {
            PortsMenu(
                canStopAll = state.hasRunning,
                onStopAll = viewModel::stopAll,
                onSettings = { navController.navigate(NavRoutes.PortForwardSettingsScreen) }
            )
        },
        fabContent = { expanded ->
            ExtendedFloatingActionButton(
                expanded = expanded,
                onClick = withHaptic { onNewTunnel() },
                icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.new_tunnel)) }
            )
        },
        content = { innerPadding, topBarScrollBehavior ->
            PortsList(
                state = state,
                listState = listState,
                contentPadding = innerPadding,
                cardActions = cardActions,
                viewModel = viewModel,
                onCreate = { navController.navigate(NavRoutes.TunnelEditorScreen()) },
                onMoreOptions = onNewTunnel,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            )
        }
    )

    if (showKindPicker) {
        KindPickerSheet(
            devices = state.connectedDevices,
            onNetwork = {
                showKindPicker = false
                navController.navigate(NavRoutes.TunnelEditorScreen())
            },
            onDevice = { transport ->
                showKindPicker = false
                navController.navigate(NavRoutes.TunnelEditorScreen(transport = transport))
            },
            onDismiss = { showKindPicker = false }
        )
    }

    selectedId?.let { id -> state.item(id) }?.let { item ->
        TunnelDetailSheet(
            item = item,
            actions = TunnelDetailActions(
                onRunningChange = { cardActions(item).onRunningChange(it) },
                onEdit = {
                    selectedId = null
                    navController.navigate(NavRoutes.TunnelEditorScreen(item.spec.id))
                },
                onOpen = { UrlUtils.openUrl(item.spec.browserUrl, context) },
                onCopyActivity = { ClipboardUtils.copyToClipboard(it, context) },
                onDelete = {
                    selectedId = null
                    viewModel.delete(item.spec)
                },
                onDismiss = { selectedId = null }
            )
        )
    }
}

@Composable
private fun PortsList(
    state: PortsUiState,
    listState: LazyListState,
    contentPadding: PaddingValues,
    cardActions: (TunnelItem) -> TunnelCardActions,
    viewModel: PortsViewModel,
    onCreate: () -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notificationsAllowed = rememberNotificationsAllowed()
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = HorizontalPadding,
            end = HorizontalPadding,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + FAB_CLEARANCE
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "scopes") {
            ScopeChips(scope = state.scope, devices = state.connectedDevices, onScopeChange = viewModel::setScope)
        }
        if (state.interrupted.isNotEmpty()) {
            item(key = "interrupted") { InterruptedBanner(state, viewModel) }
        }
        state.scopedDevice?.let { device ->
            item(key = "quick_forward") { QuickForward(device, viewModel, onMoreOptions) }
        }
        if (!notificationsAllowed && state.hasRunning) {
            item(key = "notifications") {
                PortsBanner(
                    icon = R.drawable.ic_notifications,
                    message = stringResource(R.string.port_forward_notifications_off),
                    primary = BannerAction(stringResource(R.string.allow)) {
                        context.startActivity(createAppNotificationSettingsIntent(context))
                    }
                )
            }
        }
        if (state.isEmpty && state.scope !is PortsScope.Device) {
            item(key = "empty") { PortsEmptyState(onCreateNetworkTunnel = onCreate) }
        }
        tunnelSection(R.string.running, state.running, cardActions)
        tunnelSection(R.string.port_forward_saved, state.saved, cardActions)
    }
}

@Composable
private fun QuickForward(device: ExternalDeviceState.Connected, viewModel: PortsViewModel, onMoreOptions: () -> Unit) {
    val context = LocalContext.current
    QuickForwardRow(
        device = device,
        onForward = { port -> viewModel.quickForward(port, context.getString(R.string.port_forward_quick_name, port)) },
        onPreset = { preset -> viewModel.quickPreset(preset, context.getString(preset.labelRes())) },
        onMoreOptions = onMoreOptions
    )
}

@Composable
private fun InterruptedBanner(state: PortsUiState, viewModel: PortsViewModel) {
    PortsBanner(
        icon = R.drawable.ic_warning,
        message = pluralStringResource(
            R.plurals.port_forward_interrupted,
            state.interrupted.size,
            state.interrupted.size
        ),
        primary = BannerAction(stringResource(R.string.restart), viewModel::restartInterrupted),
        secondary = BannerAction(stringResource(R.string.dismiss), viewModel::dismissInterrupted)
    )
}

private fun LazyListScope.tunnelSection(
    titleRes: Int,
    items: List<TunnelItem>,
    cardActions: (TunnelItem) -> TunnelCardActions
) {
    if (items.isEmpty()) return
    item(key = "header_$titleRes") {
        EditorSectionLabel(stringResource(titleRes))
    }
    itemsIndexed(items, key = { _, item -> item.spec.id }) { index, item ->
        TunnelCard(
            modifier = Modifier.animateItem(),
            item = item,
            index = index,
            count = items.size,
            actions = cardActions(item)
        )
    }
}

@Composable
private fun PortsMenu(canStopAll: Boolean, onStopAll: () -> Unit, onSettings: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = withHaptic { expanded = true }) {
        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.more))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        if (canStopAll) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.stop_all)) },
                onClick = {
                    expanded = false
                    onStopAll()
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings), color = MaterialTheme.colorScheme.onSurface) },
            onClick = {
                expanded = false
                onSettings()
            }
        )
    }
}

private fun copyText(spec: TunnelSpec): String = if (spec.looksLikeHttp) spec.browserUrl else spec.localAddress

private fun handleEvent(event: PortsEvent, viewModel: PortsViewModel, context: Context, snackBar: SnackBarController) {
    when (event) {
        is PortsEvent.Started -> Unit
        is PortsEvent.Deleted -> snackBar.show(
            message = context.getString(R.string.port_forward_deleted, event.spec.name),
            actionText = context.getString(R.string.undo),
            onActionClicked = { viewModel.restore(event.spec, start = event.wasRunning) }
        )

        is PortsEvent.StoppedAll -> snackBar.show(
            message = context.resources.getQuantityString(
                R.plurals.port_forward_stopped_all,
                event.specs.size,
                event.specs.size
            ),
            actionText = context.getString(R.string.undo),
            onActionClicked = { viewModel.startAll(event.specs) }
        )
    }
}

private val FAB_CLEARANCE = 96.dp
