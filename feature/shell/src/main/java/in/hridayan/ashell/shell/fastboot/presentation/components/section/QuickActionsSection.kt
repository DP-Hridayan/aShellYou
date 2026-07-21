@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionsSection(
    isConnected: Boolean,
    onReboot: (RebootMode) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingExtraLarge)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.fastboot_quick_actions),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = stringResource(R.string.reboot_normal),
                    icon = Icons.Default.PowerSettingsNew,
                    enabled = isConnected,
                    onClick = { onReboot(RebootMode.NORMAL) }
                )
                QuickActionChip(
                    label = stringResource(R.string.reboot_bootloader),
                    icon = Icons.Default.Refresh,
                    enabled = isConnected,
                    onClick = { onReboot(RebootMode.BOOTLOADER) }
                )
                QuickActionChip(
                    label = stringResource(R.string.reboot_recovery),
                    icon = Icons.Default.SettingsBackupRestore,
                    enabled = isConnected,
                    onClick = { onReboot(RebootMode.RECOVERY) }
                )
                QuickActionChip(
                    label = stringResource(R.string.reboot_fastbootd),
                    icon = Icons.Default.PhoneAndroid,
                    enabled = isConnected,
                    onClick = { onReboot(RebootMode.FASTBOOTD) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = withHaptic(HapticFeedbackType.Confirm) { onClick() },
        enabled = enabled,
        modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
