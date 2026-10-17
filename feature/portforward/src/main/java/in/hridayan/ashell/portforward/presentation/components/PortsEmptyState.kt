package `in`.hridayan.ashell.portforward.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

/** Shown before the first tunnel exists: what the feature does, and the way to start. */
@Composable
fun PortsEmptyState(onCreateNetworkTunnel: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(56.dp),
            painter = painterResource(R.drawable.ic_port_forward),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.port_forward_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        NetworkKindCard(onClick = onCreateNetworkTunnel)
    }
}

@Composable
private fun NetworkKindCard(onClick: () -> Unit) {
    CustomCard(modifier = Modifier.fillMaxWidth(), onClick = withHaptic { onClick() }) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.ic_language), contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.port_forward_kind_network),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.port_forward_kind_network_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
