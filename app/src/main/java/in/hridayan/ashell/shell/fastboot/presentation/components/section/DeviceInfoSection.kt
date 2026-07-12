@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceInfoSection(
    deviceInfo: FastbootDeviceInfo?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CustomCardShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingExtraLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.fastboot_device_info),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                if (isConnected) {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            AnimatedVisibility(visible = !isLoading && !isConnected) {
                Text(
                    text = stringResource(R.string.no_device_connected),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.6f)
                )
            }

            AnimatedVisibility(visible = !isLoading && isConnected && deviceInfo != null) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    deviceInfo?.let { info ->
                        val rows = listOf(
                            stringResource(R.string.product) to info.product,
                            stringResource(R.string.serial_number) to info.serialNo,
                            "Variant" to info.variant,
                            stringResource(R.string.bootloader_version) to info.bootloaderVersion,
                            stringResource(R.string.baseband_version) to info.basebandVersion,
                            stringResource(R.string.unlock_status) to info.isUnlocked?.let {
                                if (it) stringResource(R.string.unlocked)
                                else stringResource(R.string.locked)
                            },
                            stringResource(R.string.active_slot) to info.currentSlot,
                            stringResource(R.string.battery_level) to info.batteryLevel,
                            stringResource(R.string.max_download_size) to info.maxDownloadSize,
                            stringResource(R.string.security_patch) to info.securityPatchLevel
                        )

                        rows.forEachIndexed { index, (label, value) ->
                            if (value != null) {
                                DeviceInfoRow(
                                    label = label,
                                    value = value,
                                    onLongClick = {
                                        copyToClipboard(context, label, value)
                                    }
                                )
                                if (index < rows.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.alpha(0.15f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
}
