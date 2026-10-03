@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

/**
 * A command for the user to run elsewhere, shown in a highlighted, selectable block with a copy
 * button. Copying is left to [onCopy] so the caller decides how to confirm it.
 *
 * The command is always monospace, whatever font the user picked for the app, so it reads as
 * something to type exactly.
 */
@Composable
fun CopyableCommandBlock(
    command: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = command,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmallEmphasized,
                color = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = withHaptic { onCopy() }) {
            Icon(
                painter = painterResource(R.drawable.ic_copy),
                contentDescription = stringResource(R.string.copy)
            )
        }
    }
}
