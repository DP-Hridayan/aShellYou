package `in`.hridayan.ashell.mirror.presentation.components.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ChangeHistory
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.SwipeDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.DeviceKey

private const val BACK_TRIANGLE_ROTATION = -90f
private val RowGap = 8.dp
private val KeyGap = 4.dp
private val RowPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
private val RailPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
private val RowElevation = 3.dp

/**
 * The device's keys in two compact rows, stacked under the video in portrait and side by side as
 * rails in landscape: navigation (Back, Home, Recents) in one, the remaining device actions in the
 * other. The rows use the floating toolbar's shape and colours but not the component itself, whose
 * 64 dp minimum is far too tall for keys that sit beside a live screen.
 *
 * Back, Home and Recents use the shapes of Android's own navigation bar, so the device's Back is
 * never mistaken for the arrow that leaves the mirror.
 */
@Composable
fun DeviceKeyBar(
    vertical: Boolean,
    enabled: Boolean,
    onKey: (DeviceKey) -> Unit,
    onExpandQuickSettings: () -> Unit,
    modifier: Modifier = Modifier,
    contained: Boolean = true
) {
    val description = stringResource(R.string.device_keys)
    val barModifier = modifier.semantics { contentDescription = description }

    if (vertical) {
        Row(modifier = barModifier, horizontalArrangement = Arrangement.spacedBy(RowGap)) {
            KeyGroup(vertical = true, contained = contained) { NavigationKeys(enabled, onKey) }
            KeyGroup(vertical = true, contained = contained) { ActionKeys(enabled, onKey, onExpandQuickSettings) }
        }
    } else {
        Column(
            modifier = barModifier,
            verticalArrangement = Arrangement.spacedBy(RowGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KeyGroup(vertical = false, contained = contained) { NavigationKeys(enabled, onKey) }
            KeyGroup(vertical = false, contained = contained) { ActionKeys(enabled, onKey, onExpandQuickSettings) }
        }
    }
}

/**
 * One row of keys. When not [contained], the row has no container of its own, because it sits
 * inside a panel that already provides one.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KeyGroup(vertical: Boolean, contained: Boolean, content: @Composable () -> Unit) {
    if (!contained) {
        KeyGroupLayout(vertical, content)
        return
    }
    val colors = FloatingToolbarDefaults.standardFloatingToolbarColors()
    Surface(
        shape = FloatingToolbarDefaults.ContainerShape,
        color = colors.toolbarContainerColor,
        contentColor = colors.toolbarContentColor,
        shadowElevation = RowElevation
    ) {
        KeyGroupLayout(vertical, content)
    }
}

@Composable
private fun KeyGroupLayout(vertical: Boolean, content: @Composable () -> Unit) {
    if (vertical) {
        Column(
            modifier = Modifier.padding(RailPadding),
            verticalArrangement = Arrangement.spacedBy(KeyGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    } else {
        Row(
            modifier = Modifier.padding(RowPadding),
            horizontalArrangement = Arrangement.spacedBy(KeyGap),
            verticalAlignment = Alignment.CenterVertically
        ) { content() }
    }
}

@Composable
private fun NavigationKeys(enabled: Boolean, onKey: (DeviceKey) -> Unit) {
    KeyButton(
        icon = Icons.Rounded.ChangeHistory,
        label = stringResource(R.string.back),
        enabled = enabled,
        iconModifier = Modifier.rotate(BACK_TRIANGLE_ROTATION),
        onClick = { onKey(DeviceKey.BACK) }
    )
    HomeKeyButton(enabled = enabled, onClick = { onKey(DeviceKey.HOME) })
    KeyButton(
        icon = Icons.Rounded.CropSquare,
        label = stringResource(R.string.recents),
        enabled = enabled,
        onClick = { onKey(DeviceKey.RECENTS) }
    )
}

@Composable
private fun ActionKeys(enabled: Boolean, onKey: (DeviceKey) -> Unit, onExpandQuickSettings: () -> Unit) {
    KeyButton(
        icon = Icons.AutoMirrored.Rounded.VolumeDown,
        label = stringResource(R.string.volume_down),
        enabled = enabled,
        onClick = { onKey(DeviceKey.VOLUME_DOWN) }
    )
    KeyButton(
        icon = Icons.AutoMirrored.Rounded.VolumeUp,
        label = stringResource(R.string.volume_up),
        enabled = enabled,
        onClick = { onKey(DeviceKey.VOLUME_UP) }
    )
    KeyButton(
        icon = Icons.Rounded.PowerSettingsNew,
        label = stringResource(R.string.power),
        enabled = enabled,
        onClick = { onKey(DeviceKey.POWER) }
    )
    KeyButton(
        icon = Icons.Rounded.Screenshot,
        label = stringResource(R.string.screenshot),
        enabled = enabled,
        onClick = { onKey(DeviceKey.SCREENSHOT) }
    )
    KeyButton(
        icon = Icons.Rounded.SwipeDown,
        label = stringResource(R.string.quick_settings),
        enabled = enabled,
        onClick = onExpandQuickSettings
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeKeyButton(enabled: Boolean, onClick: () -> Unit) {
    val label = stringResource(R.string.home)
    TooltipContent(text = label) {
        FilledIconButton(
            onClick = withHaptic(HapticFeedbackType.VirtualKey, onClick),
            enabled = enabled,
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(
                IconButtonDefaults.extraSmallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.RadioButtonUnchecked,
                contentDescription = label,
                modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KeyButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    iconModifier: Modifier = Modifier
) {
    TooltipContent(text = label) {
        IconButton(
            onClick = withHaptic(HapticFeedbackType.VirtualKey, onClick),
            enabled = enabled,
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize())
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = iconModifier.size(IconButtonDefaults.extraSmallIconSize)
            )
        }
    }
}
