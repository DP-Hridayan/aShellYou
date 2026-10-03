@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.TcpIpEnableResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.model.TcpIpEnableUiState

/**
 * Reports opening ADB's TCP port on [port], from waiting for pairing through to the result. [onBack]
 * is offered for every result except a confirmed open, which the success dialog handles instead.
 */
@Composable
fun TcpIpEnableCard(
    state: TcpIpEnableUiState,
    port: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuccess = state is TcpIpEnableUiState.Finished && state.result.isSuccess()
    val isFailure = state is TcpIpEnableUiState.Finished && !state.result.isSuccess()

    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(iconFor(isSuccess, isFailure)),
        text = messageFor(state, port),
        colors = colorsFor(isSuccess, isFailure)
    ) {
        if (state == TcpIpEnableUiState.Running) {
            LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (state is TcpIpEnableUiState.Finished && state.result !is TcpIpEnableResult.Opened) {
            Button(shapes = ButtonDefaults.shapes(), onClick = withHaptic { onBack() }) {
                AutoResizeableText(text = stringResource(R.string.back))
            }
        }
    }
}

private fun TcpIpEnableResult.isSuccess(): Boolean =
    this is TcpIpEnableResult.Opened || this == TcpIpEnableResult.Unverified

private fun iconFor(isSuccess: Boolean, isFailure: Boolean): Int = when {
    isSuccess -> R.drawable.ic_check_circle
    isFailure -> R.drawable.ic_error
    else -> R.drawable.ic_info
}

@Composable
private fun messageFor(state: TcpIpEnableUiState, port: Int): String = when (state) {
    TcpIpEnableUiState.Idle -> stringResource(R.string.tcpip_enable_pending_hint, port)
    TcpIpEnableUiState.Running -> stringResource(R.string.tcpip_enable_running, port)
    is TcpIpEnableUiState.Finished -> when (val result = state.result) {
        is TcpIpEnableResult.Opened -> stringResource(R.string.tcpip_enable_opened, result.port)
        TcpIpEnableResult.Unverified -> stringResource(R.string.tcpip_enable_unverified, port)
        TcpIpEnableResult.NotConnected -> stringResource(R.string.adb_not_connected)
        is TcpIpEnableResult.Refused -> stringResource(R.string.tcpip_enable_refused, result.message)
    }
}

@Composable
private fun colorsFor(isSuccess: Boolean, isFailure: Boolean): CardColors = when {
    isFailure -> CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    )

    isSuccess -> CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    else -> CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}
