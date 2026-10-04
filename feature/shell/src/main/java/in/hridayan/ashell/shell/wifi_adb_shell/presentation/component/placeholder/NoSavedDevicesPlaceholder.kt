package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R

private const val ICON_ALPHA = 0.5f
private const val HINT_ALPHA = 0.7f

/**
 * Shown in place of the saved device list when nothing has been paired. [action], when given, sits
 * below the [hint] so a caller can offer pairing right there.
 */
@Composable
fun NoSavedDevicesPlaceholder(
    hint: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_wireless),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ICON_ALPHA),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.no_saved_devices),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HINT_ALPHA),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        action?.let {
            Spacer(modifier = Modifier.height(24.dp))
            it()
        }
    }
}
