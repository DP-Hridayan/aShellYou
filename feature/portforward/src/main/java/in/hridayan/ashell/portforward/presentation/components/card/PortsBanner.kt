package `in`.hridayan.ashell.portforward.presentation.components.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.portforward.presentation.model.BannerAction

/**
 * A message above the tunnel list about something the user should act on, with up to two actions:
 * [primary] fixes it, [secondary] usually dismisses it.
 */
@Composable
fun PortsBanner(
    @DrawableRes icon: Int,
    message: String,
    primary: BannerAction,
    modifier: Modifier = Modifier,
    secondary: BannerAction? = null
) {
    CustomCard(
        modifier = modifier.fillMaxWidth(),
        clickable = false,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Icon(modifier = Modifier.size(20.dp), painter = painterResource(icon), contentDescription = null)
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                secondary?.let { TextButton(onClick = withHaptic { it.onClick() }) { Text(it.label) } }
                FilledTonalButton(onClick = withHaptic { primary.onClick() }) { Text(primary.label) }
            }
        }
    }
}
