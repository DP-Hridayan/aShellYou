@file:Suppress("UNCHECKED_CAST")

package `in`.hridayan.ashell.ui.component.button

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun IconWithTextButton(
    icon: Painter,
    text: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.animateContentSize(),
    onClick: () -> Unit = {}
) {
    Button(onClick = onClick) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}