package `in`.hridayan.ashell.mirror.presentation.screens

import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.presentation.components.controls.DeviceKeyBar
import `in`.hridayan.ashell.mirror.presentation.components.controls.FullscreenControlActions
import `in`.hridayan.ashell.mirror.presentation.components.controls.FullscreenControls
import `in`.hridayan.ashell.mirror.presentation.components.controls.MirrorTopBar
import `in`.hridayan.ashell.mirror.presentation.components.controls.MirrorTopBarState
import `in`.hridayan.ashell.mirror.presentation.components.quality.StreamStatsOverlay
import `in`.hridayan.ashell.mirror.presentation.components.quality.VideoQualitySheet
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorEndedDialog
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorFailedDialog
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorReconnectingScrim
import `in`.hridayan.ashell.mirror.presentation.components.state.MirrorStartupPanel
import `in`.hridayan.ashell.mirror.presentation.components.video.MirrorVideoStage
import `in`.hridayan.ashell.mirror.presentation.model.FullscreenActions
import `in`.hridayan.ashell.mirror.presentation.model.MirrorContentActions
import `in`.hridayan.ashell.mirror.presentation.model.MirrorUiState

private val KeyBarPadding = 12.dp
private val StatsOverlayMargin = 8.dp

/**
 * What the mirror's chrome needs besides the session, worked out once per composition.
 *
 * @property canEnterFullscreen video is showing and the window isn't in multi-window mode, where
 * immersive mode doesn't apply.
 */
private data class ChromeState(
    val isFullscreen: Boolean,
    val canEnterFullscreen: Boolean,
    val isViewOnly: Boolean,
    val keysEnabled: Boolean
)

/** The screen's actions plus the dialogs and sheets this content opens itself. */
private data class LayoutCallbacks(
    val actions: MirrorContentActions,
    val onViewOnlyInfo: () -> Unit,
    val onOpenQuality: () -> Unit
)

/**
 * Lays the mirror out for the window's shape: top bar, video and key rows when the window is
 * taller than wide; video and a key rail beside it otherwise. Fullscreen hides that chrome and the
 * system bars, and adds the floating controls.
 *
 * Fullscreen is the same layout with its chrome removed rather than a separate branch, so the video
 * stage stays at the same place in the composition and its surface, with its decoder, survives the
 * switch instead of blanking the picture while both restart.
 */
@Composable
fun MirrorContent(
    uiState: MirrorUiState,
    actions: MirrorContentActions,
    isFullscreenAllowed: Boolean,
    modifier: Modifier = Modifier
) {
    var showViewOnlyInfo by rememberSaveable { mutableStateOf(false) }
    var showQualitySheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val streaming = uiState.session as? MirrorState.Streaming
    val chrome = ChromeState(
        isFullscreen = uiState.isFullscreen,
        canEnterFullscreen = isFullscreenAllowed && streaming?.videoSize != null,
        isViewOnly = streaming?.isControlAvailable == false,
        keysEnabled = streaming?.isControlAvailable == true
    )

    val callbacks = LayoutCallbacks(
        actions = actions,
        onViewOnlyInfo = { showViewOnlyInfo = true },
        onOpenQuality = {
            actions.fullscreen.onDismissControlPanel()
            showQualitySheet = true
        }
    )

    FullscreenBackHandler(uiState, actions.fullscreen)
    FullscreenHint(chrome.isFullscreen, snackbarHostState, actions.fullscreen::onFullscreenHintShown)
    QualityHints(uiState.quality, snackbarHostState, actions.quality)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val isWide = maxWidth > maxHeight
        val layoutModifier = if (chrome.isFullscreen) {
            Modifier
        } else {
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        }

        if (isWide) {
            WideLayout(uiState, chrome, callbacks, layoutModifier)
        } else {
            TallLayout(uiState, chrome, callbacks, layoutModifier)
        }

        if (chrome.isFullscreen) {
            FullscreenControls(
                isPanelOpen = uiState.chrome.isPanelOpen,
                keysEnabled = chrome.keysEnabled,
                isWide = isWide,
                actions = FullscreenControlActions(
                    onTogglePanel = actions.fullscreen::onToggleControlPanel,
                    onDismissPanel = actions.fullscreen::onDismissControlPanel,
                    onKey = actions.mirror::onDeviceKey,
                    onExpandQuickSettings = actions.mirror::onExpandQuickSettings,
                    onOpenQuality = callbacks.onOpenQuality,
                    onExitFullscreen = actions.fullscreen::onExitFullscreen,
                    onLeave = actions.onLeave
                )
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        )
    }

    if (showViewOnlyInfo) {
        ViewOnlyDialog(onDismiss = { showViewOnlyInfo = false })
    }

    if (showQualitySheet) {
        VideoQualitySheet(
            state = uiState.quality,
            preview = actions.quality::previewQuality,
            onApply = actions.quality::onApplyQuality,
            onStatsOverlayChange = actions.quality::onStatsOverlayChange,
            onDismiss = { showQualitySheet = false }
        )
    }
}

@Composable
private fun WideLayout(
    uiState: MirrorUiState,
    chrome: ChromeState,
    callbacks: LayoutCallbacks,
    modifier: Modifier
) {
    val actions = callbacks.actions.mirror
    Row(modifier = modifier) {
        MirrorStage(uiState, callbacks.actions, Modifier.weight(1f))
        if (!chrome.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(KeyBarPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RailButtons(chrome, callbacks)
                Spacer(Modifier.weight(1f))
                DeviceKeyBar(
                    vertical = true,
                    enabled = chrome.keysEnabled,
                    onKey = actions::onDeviceKey,
                    onExpandQuickSettings = actions::onExpandQuickSettings,
                    onOpenQuality = callbacks.onOpenQuality
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColumnScope.RailButtons(chrome: ChromeState, callbacks: LayoutCallbacks) {
    IconButton(onClick = withHaptic(block = callbacks.actions.onLeave)) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.back_to_shell)
        )
    }
    IconButton(
        onClick = withHaptic(block = callbacks.actions.fullscreen::onEnterFullscreen),
        enabled = chrome.canEnterFullscreen
    ) {
        Icon(imageVector = Icons.Rounded.Fullscreen, contentDescription = stringResource(R.string.full_screen))
    }
    if (chrome.isViewOnly) {
        IconButton(onClick = callbacks.onViewOnlyInfo) {
            Icon(Icons.Rounded.Lock, contentDescription = stringResource(R.string.view_only))
        }
    }
}

@Composable
private fun TallLayout(
    uiState: MirrorUiState,
    chrome: ChromeState,
    callbacks: LayoutCallbacks,
    modifier: Modifier
) {
    val actions = callbacks.actions.mirror
    Column(modifier = modifier) {
        if (!chrome.isFullscreen) {
            MirrorTopBar(
                state = MirrorTopBarState(
                    deviceName = (uiState.session as? MirrorState.Streaming)?.deviceName,
                    transport = uiState.transport,
                    isViewOnly = chrome.isViewOnly,
                    canEnterFullscreen = chrome.canEnterFullscreen
                ),
                onLeave = callbacks.actions.onLeave,
                onViewOnlyInfo = callbacks.onViewOnlyInfo,
                onEnterFullscreen = callbacks.actions.fullscreen::onEnterFullscreen
            )
        }
        MirrorStage(uiState, callbacks.actions, Modifier.weight(1f))
        if (!chrome.isFullscreen) {
            DeviceKeyBar(
                vertical = false,
                enabled = chrome.keysEnabled,
                onKey = actions::onDeviceKey,
                onExpandQuickSettings = actions::onExpandQuickSettings,
                onOpenQuality = callbacks.onOpenQuality,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(KeyBarPadding)
            )
        }
    }
}

/**
 * In fullscreen, Back closes an open panel first, then goes to the device while it can take input,
 * and otherwise leaves fullscreen. It never leaves the mirror from fullscreen, so the user always
 * lands in the windowed layout first. With TalkBack or another touch-exploration service on, Back
 * is how those users get around, so it stays with the app.
 */
@Composable
private fun FullscreenBackHandler(uiState: MirrorUiState, actions: FullscreenActions) {
    val context = LocalContext.current
    BackHandler(enabled = uiState.isFullscreen) {
        val accessibility = context.getSystemService(AccessibilityManager::class.java)
        val isTouchExplorationOn = accessibility?.isTouchExplorationEnabled == true
        when {
            uiState.chrome.isPanelOpen -> actions.onDismissControlPanel()
            uiState.backGoesToDevice(isTouchExplorationOn) -> actions.onHostBack()
            else -> actions.onExitFullscreen()
        }
    }
}

/** Explains, once ever, that Back now goes to the device and where the controls went. */
@Composable
private fun FullscreenHint(isFullscreen: Boolean, hostState: SnackbarHostState, onShown: () -> Unit) {
    val alreadyShown = LocalSettings.current[SettingsKeys.MirrorFullscreenHintShown]
    val message = stringResource(R.string.mirror_full_screen_hint)
    LaunchedEffect(isFullscreen) {
        if (!isFullscreen || alreadyShown) return@LaunchedEffect
        onShown()
        hostState.showSnackbar(message)
    }
}

/** The video plus whatever the session state puts over it. */
@Composable
private fun MirrorStage(uiState: MirrorUiState, actions: MirrorContentActions, modifier: Modifier = Modifier) {
    val session = uiState.session
    val streaming = session as? MirrorState.Streaming
    var serverAlreadyDeployed by remember { mutableStateOf(false) }
    LaunchedEffect(session) {
        if (session is MirrorState.Starting) serverAlreadyDeployed = session.serverAlreadyDeployed
    }

    Box(modifier = modifier.fillMaxSize()) {
        MirrorVideoStage(
            videoSize = streaming?.videoSize,
            isControlAvailable = streaming?.isControlAvailable == true,
            frameRate = uiState.quality.resolved?.options?.maxFps,
            actions = actions.mirror
        )
        StatsOverlayWhenOn(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(StatsOverlayMargin)
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
                MirrorEndedDialog(reason = session.reason, onRetry = actions.mirror::onRetry, onLeave = actions.onLeave)
            }

            is MirrorState.Failed -> {
                EmptyStage()
                MirrorFailedDialog(error = session.error, onRetry = actions.mirror::onRetry, onLeave = actions.onLeave)
            }
        }
    }
}

@Composable
private fun StatsOverlayWhenOn(uiState: MirrorUiState, modifier: Modifier) {
    val quality = uiState.quality
    val stats = quality.stats ?: return
    val size = (uiState.session as? MirrorState.Streaming)?.videoSize ?: return
    val codec = quality.resolved?.options?.videoCodec ?: return
    if (quality.isStatsOverlayOn) StreamStatsOverlay(stats = stats, size = size, codec = codec, modifier = modifier)
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
