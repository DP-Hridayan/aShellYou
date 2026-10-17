@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.presentation.model.TestState
import `in`.hridayan.ashell.portforward.text.message
import `in`.hridayan.ashell.portforward.text.resolve

private val IconSize = 20.dp
private val ProgressSize = 20.dp
private val ProgressStroke = 2.dp
private val TitleGap = 4.dp

/**
 * Asks for Android's local network permission before a tunnel to the local network can work. Once
 * the system stops showing its prompt, the button opens settings instead.
 */
@Composable
fun LocalNetworkPermissionCard(permanentlyDenied: Boolean, onAllow: () -> Unit, onOpenSettings: () -> Unit) {
    NoticeCard(
        icon = R.drawable.ic_shield,
        title = stringResource(R.string.port_forward_local_network_title),
        message = stringResource(
            if (permanentlyDenied) {
                R.string.port_forward_local_network_denied
            } else {
                R.string.port_forward_local_network_description
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        actionLabel = stringResource(if (permanentlyDenied) R.string.open_settings else R.string.allow),
        onAction = if (permanentlyDenied) onOpenSettings else onAllow
    )
}

/** Why the last start failed, shown until the form changes. */
@Composable
fun StartErrorCard(error: TunnelError, spec: TunnelSpec) {
    NoticeCard(
        icon = R.drawable.ic_error,
        title = stringResource(R.string.port_forward_could_not_start),
        message = error.message(spec).resolve(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )
}

/** Checks the target before starting, and shows the last result in the same card. */
@Composable
fun TestCard(state: TestState, spec: TunnelSpec?, enabled: Boolean, onTest: () -> Unit) {
    CustomCard(modifier = Modifier.fillMaxWidth(), shape = GroupPosition.Single.card, clickable = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorSpacing.CardHorizontal, vertical = EditorSpacing.CardVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EditorSpacing.ContentGap)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalArrangement = Arrangement.spacedBy(TitleGap)
            ) {
                Text(
                    text = stringResource(R.string.port_forward_test_connection),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                AnimatedContent(targetState = state, label = "test_outcome") { current -> TestOutcome(current, spec) }
            }
            if (state == TestState.Testing) {
                CircularProgressIndicator(modifier = Modifier.size(ProgressSize), strokeWidth = ProgressStroke)
            } else {
                FilledTonalButton(enabled = enabled, onClick = withHaptic { onTest() }) {
                    Text(stringResource(R.string.test))
                }
            }
        }
    }
}

@Composable
private fun TestOutcome(state: TestState, spec: TunnelSpec?) {
    when (state) {
        TestState.Idle -> OutcomeLine(null, stringResource(R.string.port_forward_test_hint), Color.Unspecified)
        TestState.Testing -> OutcomeLine(null, stringResource(R.string.port_forward_testing), Color.Unspecified)
        is TestState.Done -> when (val result = state.result) {
            is TestResult.Reachable -> OutcomeLine(
                R.drawable.ic_check_circle,
                reachableText(result),
                MaterialTheme.colorScheme.primary
            )

            is TestResult.Unreachable -> spec?.let {
                OutcomeLine(R.drawable.ic_error, result.error.message(it).resolve(), MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun OutcomeLine(icon: Int?, text: String, color: Color) {
    val tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else color
    Row(horizontalArrangement = Arrangement.spacedBy(TitleGap), verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            Icon(
                modifier = Modifier.size(IconSize),
                painter = painterResource(it),
                contentDescription = null,
                tint = tint
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}

@Composable
private fun reachableText(result: TestResult.Reachable): String {
    val status = result.httpStatus ?: return stringResource(R.string.port_forward_test_reachable, result.latencyMillis)
    val server = result.server ?: return stringResource(R.string.port_forward_test_http, result.latencyMillis, status)
    return stringResource(R.string.port_forward_test_http_server, result.latencyMillis, status, server)
}

@Composable
private fun NoticeCard(
    icon: Int,
    title: String,
    message: String,
    colors: CardColors,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    CustomCard(
        modifier = Modifier.fillMaxWidth(),
        shape = GroupPosition.Single.card,
        clickable = false,
        colors = colors
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = EditorSpacing.CardHorizontal,
                vertical = EditorSpacing.CardVertical
            ),
            verticalArrangement = Arrangement.spacedBy(EditorSpacing.BlockGap)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EditorSpacing.BlockGap),
                verticalAlignment = Alignment.Top
            ) {
                Icon(modifier = Modifier.size(IconSize), painter = painterResource(icon), contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(TitleGap)) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall)
                    Text(text = message, style = MaterialTheme.typography.bodySmall)
                }
            }
            actionLabel?.let {
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = withHaptic { onAction() }
                ) { Text(it) }
            }
        }
    }
}
