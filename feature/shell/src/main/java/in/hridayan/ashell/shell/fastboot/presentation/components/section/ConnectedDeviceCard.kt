package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.battery.BatteryIndicator
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.divider.WavyHorizontalDivider
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs

@Composable
fun ConnectedDeviceCard(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    deviceName: String?,
    serialNumber: String?,
    variant: String?,
    bootloaderVersion: String?,
    basebandVersion: String?,
    securityPatch: String?,
    batteryLevel: Int?
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotateAngle by animateFloatAsState(if (isExpanded) 180f else 0f)

    CustomCard(
        modifier = modifier.animateContentSize(AshellYouAnimationSpecs.springIntSize),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.run {
                if (isConnected) primaryContainer else errorContainer
            },
            contentColor = MaterialTheme.colorScheme.run {
                if (isConnected) onPrimaryContainer else onErrorContainer
            },
        ),
        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
            isExpanded = !isExpanded
        },
        pressedScale = 1f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.run {
                            if (isConnected) primary else error
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.padding(15.dp),
                        painter = if (isConnected) painterResource(R.drawable.ic_check_circle)
                        else painterResource(R.drawable.ic_cancel),
                        tint = MaterialTheme.colorScheme.run {
                            if (isConnected) onPrimary else onError
                        },
                        contentDescription = null
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.connected_device),
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )

                    Text(
                        text = if (isConnected && deviceName != null) deviceName else stringResource(
                            R.string.no_device_connected
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Icon(
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotateAngle
                    },
                    painter = painterResource(`in`.hridayan.ashell.core.ui.R.drawable.ic_expand),
                    contentDescription = null
                )

                batteryLevel?.let {
                    BatteryIndicator(level = it)
                }
            }
        }

        if (isExpanded) Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            WavyHorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                waveHeight = 6.dp,
                waveLength = 30.dp,
                thickness = 1.dp
            )

            Text(
                modifier = Modifier.padding(horizontal = 15.dp),
                text = stringResource(R.string.other_details) + " : ",
                style = MaterialTheme.typography.labelLargeEmphasized,
                fontWeight = FontWeight.SemiBold
            )

            SpecificationText(
                modifier = Modifier.padding(horizontal = 15.dp),
                spec = stringResource(R.string.serial_number),
                value = serialNumber
            )

            SpecificationText(
                modifier = Modifier.padding(horizontal = 15.dp),
                spec = stringResource(R.string.variant),
                value = variant
            )

            SpecificationText(
                modifier = Modifier.padding(horizontal = 15.dp),
                spec = stringResource(R.string.bootloader_version),
                value = bootloaderVersion
            )

            SpecificationText(
                modifier = Modifier.padding(horizontal = 15.dp),
                spec = stringResource(R.string.baseband_version),
                value = basebandVersion
            )

            SpecificationText(
                modifier = Modifier.padding(horizontal = 15.dp),
                spec = stringResource(R.string.security_patch),
                value = securityPatch
            )
        }
    }
}

@Composable
private fun SpecificationText(
    modifier: Modifier = Modifier,
    spec: String,
    value: String?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$spec : ",
            style = MaterialTheme.typography.bodySmall
        )

        SelectionContainer {
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
