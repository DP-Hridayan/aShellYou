package `in`.hridayan.ashell.mirror.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.presentation.components.controls.DeviceKeyBar
import `in`.hridayan.ashell.mirror.presentation.components.controls.MirrorTopBar
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorEndedDialog
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorFailedDialog
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorReconnectingScrim
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorStartupPanel
import `in`.hridayan.ashell.mirror.presentation.components.video.MirrorVideoStage
import `in`.hridayan.ashell.mirror.presentation.model.MirrorActions
import `in`.hridayan.ashell.mirror.presentation.model.MirrorUiState

private val KeyBarPadding = 12.dp

/**
 * Lays the mirror out for the window's shape: top bar, video and a key row when the window is
 * taller than wide; video and a key rail beside it otherwise.
 */
@Composable
fun MirrorContent(
    uiState: MirrorUiState,
    actions: MirrorActions,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showViewOnlyInfo by rememberSaveable { mutableStateOf(false) }
    val streaming = uiState.session as? MirrorState.Streaming
    val isViewOnly = streaming?.isControlAvailable == false

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val isWide = maxWidth > maxHeight
        val keysEnabled = streaming?.isControlAvailable == true

        if (isWide) {
            Row(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                MirrorStage(uiState.session, actions, onLeave, Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(KeyBarPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = withHaptic(block = onLeave)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_shell)
                        )
                    }
                    if (isViewOnly) {
                        IconButton(onClick = { showViewOnlyInfo = true }) {
                            Icon(Icons.Rounded.Lock, contentDescription = stringResource(R.string.view_only))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    DeviceKeyBar(
                        vertical = true,
                        enabled = keysEnabled,
                        onKey = actions::onDeviceKey,
                        onExpandQuickSettings = actions::onExpandQuickSettings
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        } else {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                MirrorTopBar(
                    deviceName = streaming?.deviceName,
                    transport = uiState.transport,
                    isViewOnly = isViewOnly,
                    onLeave = onLeave,
                    onViewOnlyInfo = { showViewOnlyInfo = true }
                )
                MirrorStage(uiState.session, actions, onLeave, Modifier.weight(1f))
                DeviceKeyBar(
                    vertical = false,
                    enabled = keysEnabled,
                    onKey = actions::onDeviceKey,
                    onExpandQuickSettings = actions::onExpandQuickSettings,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(KeyBarPadding)
                )
            }
        }
    }

    if (showViewOnlyInfo) {
        ViewOnlyDialog(onDismiss = { showViewOnlyInfo = false })
    }
}

/** The video plus whatever the session state puts over it. */
@Composable
private fun MirrorStage(
    session: MirrorState,
    actions: MirrorActions,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streaming = session as? MirrorState.Streaming
    var serverAlreadyDeployed by remember { mutableStateOf(false) }
    LaunchedEffect(session) {
        if (session is MirrorState.Starting) serverAlreadyDeployed = session.serverAlreadyDeployed
    }

    Box(modifier = modifier.fillMaxSize()) {
        MirrorVideoStage(
            videoSize = streaming?.videoSize,
            isControlAvailable = streaming?.isControlAvailable == true,
            actions = actions
        )

        when (session) {
            MirrorState.Idle -> MirrorStartupPanel(step = null, serverAlreadyDeployed = false)

            is MirrorState.Starting -> MirrorStartupPanel(
                step = session.step,
                serverAlreadyDeployed = session.serverAlreadyDeployed
            )

            is MirrorState.Streaming -> if (session.videoSize == null) {
                MirrorStartupPanel(step = null, serverAlreadyDeployed = serverAlreadyDeployed)
            }

            MirrorState.Reconnecting -> MirrorReconnectingScrim()

            is MirrorState.Ended -> {
                EmptyStage()
                MirrorEndedDialog(reason = session.reason, onRetry = actions::onRetry, onLeave = onLeave)
            }

            is MirrorState.Failed -> {
                EmptyStage()
                MirrorFailedDialog(error = session.error, onRetry = actions::onRetry, onLeave = onLeave)
            }
        }
    }
}

/** Hides the video surface, which shows black until it has a frame, whatever the theme. */
@Composable
private fun EmptyStage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun ViewOnlyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
        title = { Text(stringResource(R.string.view_only)) },
        text = { Text(stringResource(R.string.mirror_view_only_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
