package `in`.hridayan.ashell.shell.common.presentation.components.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.presentation.model.DeviceActionId

private val DrawerWidth = 320.dp
private val DrawerHorizontalPadding = 16.dp
private val DrawerVerticalPadding = 24.dp
private val DrawerItemSpacing = 12.dp
private val CardGroupSpacing = 2.dp

private data class DrawerActionItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val badgeCount: Int = 0,
    val onClick: () -> Unit
)

@Composable
fun ShellSideDrawer(
    connectedDevice: String?,
    modeText: String,
    showLocalAdbModes: Boolean,
    actions: List<DeviceActionId>,
    badges: Map<DeviceActionId, Int> = emptyMap(),
    onAction: (DeviceActionId) -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalSettings.current
    val currentLocalAdbMode = settings[SettingsKeys.LocalAdbWorkingMode]

    val deviceItems = buildList {
        if (actions.contains(DeviceActionId.SCREEN)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.screen),
                    icon = { ActionIcon(action = DeviceActionId.SCREEN) },
                    badgeCount = badges[DeviceActionId.SCREEN] ?: 0,
                    onClick = { onAction(DeviceActionId.SCREEN) }
                )
            )
        }
        if (actions.contains(DeviceActionId.FILES)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.file_browser),
                    icon = { ActionIcon(action = DeviceActionId.FILES) },
                    badgeCount = badges[DeviceActionId.FILES] ?: 0,
                    onClick = { onAction(DeviceActionId.FILES) }
                )
            )
        }
        if (actions.contains(DeviceActionId.PORTS)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.port_forwarding),
                    icon = { ActionIcon(action = DeviceActionId.PORTS) },
                    badgeCount = badges[DeviceActionId.PORTS] ?: 0,
                    onClick = { onAction(DeviceActionId.PORTS) }
                )
            )
        }
    }

    val outputItems = buildList {
        if (actions.contains(DeviceActionId.SHARE_OUTPUT)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.share),
                    icon = { ActionIcon(action = DeviceActionId.SHARE_OUTPUT) },
                    onClick = { onAction(DeviceActionId.SHARE_OUTPUT) }
                )
            )
        }
        if (actions.contains(DeviceActionId.SAVE_OUTPUT)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.save),
                    icon = { ActionIcon(action = DeviceActionId.SAVE_OUTPUT) },
                    onClick = { onAction(DeviceActionId.SAVE_OUTPUT) }
                )
            )
        }
    }

    val appItems = buildList {
        if (actions.contains(DeviceActionId.ASK_AI)) {
            add(
                DrawerActionItem(
                    title = stringResource(R.string.adb_agent),
                    icon = { ActionIcon(action = DeviceActionId.ASK_AI) },
                    onClick = { onAction(DeviceActionId.ASK_AI) }
                )
            )
        }
        add(
            DrawerActionItem(
                title = stringResource(R.string.settings),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings)
                    )
                },
                onClick = onNavigateSettings
            )
        )
    }

    ModalDrawerSheet(
        modifier = modifier
            .width(DrawerWidth)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DrawerHorizontalPadding, vertical = DrawerVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(DrawerItemSpacing)
        ) {
            ConnectedDeviceDrawerCard(
                connectedDevice = connectedDevice,
                modeText = modeText
            )

            if (showLocalAdbModes) {
                LocalAdbModeCollapsibleCard(
                    selectedMode = currentLocalAdbMode,
                    onModeSelected = { newMode ->
                        settings.set(SettingsKeys.LocalAdbWorkingMode, newMode)
                    }
                )
            }

            if (deviceItems.isNotEmpty()) {
                ActionCardGroup(actions = deviceItems)
            }

            if (outputItems.isNotEmpty()) {
                ActionCardGroup(actions = outputItems)
            }

            ActionCardGroup(actions = appItems)
        }
    }
}

@Composable
private fun ActionCardGroup(
    actions: List<DrawerActionItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CardGroupSpacing)
    ) {
        actions.forEachIndexed { index, item ->
            DrawerActionCard(
                title = item.title,
                icon = item.icon,
                badgeCount = item.badgeCount,
                onClick = item.onClick,
                shape = CardCornerShape.getRoundedShape(index = index, size = actions.size)
            )
        }
    }
}

@Composable
private fun ActionIcon(
    action: DeviceActionId,
    modifier: Modifier = Modifier
) {
    val label = stringResource(action.labelResId())
    when (action) {
        DeviceActionId.SCREEN -> Icon(
            imageVector = Icons.Rounded.CastConnected,
            contentDescription = label,
            modifier = modifier
        )

        DeviceActionId.FILES -> Icon(
            painter = painterResource(R.drawable.ic_directory),
            contentDescription = label,
            modifier = modifier
        )

        DeviceActionId.PORTS -> Icon(
            painter = painterResource(R.drawable.ic_port_forward),
            contentDescription = label,
            modifier = modifier
        )

        DeviceActionId.SHARE_OUTPUT -> Icon(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = label,
            modifier = modifier
        )

        DeviceActionId.SAVE_OUTPUT -> Icon(
            painter = painterResource(R.drawable.ic_save),
            contentDescription = label,
            modifier = modifier
        )

        DeviceActionId.ASK_AI -> Icon(
            painter = painterResource(R.drawable.ic_help),
            contentDescription = label,
            modifier = modifier
        )
    }
}

private fun DeviceActionId.labelResId(): Int = when (this) {
    DeviceActionId.SCREEN -> R.string.screen
    DeviceActionId.FILES -> R.string.file_browser
    DeviceActionId.PORTS -> R.string.port_forwarding
    DeviceActionId.SHARE_OUTPUT -> R.string.share
    DeviceActionId.SAVE_OUTPUT -> R.string.save
    DeviceActionId.ASK_AI -> R.string.adb_agent
}
