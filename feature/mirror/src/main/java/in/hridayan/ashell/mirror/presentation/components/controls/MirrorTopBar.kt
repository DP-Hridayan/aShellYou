package `in`.hridayan.ashell.mirror.presentation.components.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

/**
 * @param deviceName null until the device has introduced itself.
 * @param isViewOnly shows a badge when the device refused input, so a touch that does nothing has a
 * visible reason.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MirrorTopBar(
    deviceName: String?,
    transport: ExternalDeviceTransport,
    isViewOnly: Boolean,
    onLeave: () -> Unit,
    onViewOnlyInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = withHaptic(block = onLeave)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_shell)
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = deviceName ?: stringResource(R.string.screen_mirror),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(transport.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            if (isViewOnly) {
                AssistChip(
                    onClick = withHaptic(block = onViewOnlyInfo),
                    label = { Text(stringResource(R.string.view_only)) },
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) }
                )
            }
        }
    )
}

private fun ExternalDeviceTransport.labelRes(): Int = when (this) {
    ExternalDeviceTransport.OTG -> R.string.otg
    ExternalDeviceTransport.WIFI_ADB -> R.string.wifi_adb
}
