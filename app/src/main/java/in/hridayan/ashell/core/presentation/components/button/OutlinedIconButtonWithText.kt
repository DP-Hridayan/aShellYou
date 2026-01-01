@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun OutlinedIconButtonWithText(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    painter: Painter
) {
    OutlinedButton(
        modifier = modifier,
        onClick = withHaptic{
            onClick()
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        ),
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(10.dp))
        AutoResizeableText(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}