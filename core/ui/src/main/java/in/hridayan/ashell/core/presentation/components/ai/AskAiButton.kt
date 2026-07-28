package `in`.hridayan.ashell.core.presentation.components.ai

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

@Composable
fun AskAiButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = withHaptic {
            onClick()
        },
        modifier = modifier,
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.IconSize),
            painter = painterResource(R.drawable.ic_help),
            contentDescription = null,
        )

        Spacer(Modifier.widthIn(ButtonDefaults.IconSpacing))

        Text(text = stringResource(R.string.adb_copilot))
    }
}
