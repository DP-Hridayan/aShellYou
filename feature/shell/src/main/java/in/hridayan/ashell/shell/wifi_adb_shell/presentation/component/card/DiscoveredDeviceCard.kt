@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.CodePairingStage
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.inputfield.DigitBoxInputField

private const val PAIRING_CODE_LENGTH = 6
private val PROGRESS_SIZE = 16.dp
private val PROGRESS_STROKE = 2.dp
private val PROGRESS_SPACING = 10.dp

private fun CodePairingStage.labelRes(): Int = when (this) {
    CodePairingStage.Idle -> R.string.pair
    CodePairingStage.Pairing -> R.string.pairing_in_progress
    CodePairingStage.Connecting -> R.string.connecting
}

/**
 * Card displayed for each discovered pairing service.
 *
 * The button reports the stage the attempt has reached rather than only whether one is running, and a
 * rejected code is shown beneath the input rather than only as a dialog several seconds later.
 */
@Composable
fun DiscoveredDeviceCard(
    modifier: Modifier = Modifier,
    service: DiscoveredPairingService,
    stage: CodePairingStage = CodePairingStage.Idle,
    errorMessage: String? = null,
    onPair: (pairingCode: String) -> Unit = {},
    onCodeChange: () -> Unit = {}
) {
    var pairingCode by remember { mutableStateOf("") }
    val isBusy = stage != CodePairingStage.Idle

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wireless),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${service.ip}:${service.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // OTP Input
            Text(
                text = stringResource(R.string.enter_pairing_code),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DigitBoxInputField(
                value = pairingCode,
                onValueChange = {
                    pairingCode = it
                    onCodeChange()
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null && !isBusy) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = withHaptic { onPair(pairingCode) },
                enabled = pairingCode.length == PAIRING_CODE_LENGTH && !isBusy,
                modifier = Modifier.fillMaxWidth(),
                shapes = ButtonDefaults.shapes()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PROGRESS_SPACING),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(PROGRESS_SIZE),
                            strokeWidth = PROGRESS_STROKE,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    AutoResizeableText(text = stringResource(stage.labelRes()))
                }
            }
        }
    }
}
