package `in`.hridayan.ashell.core.presentation.components.text

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun BulletPointsTextLayout(
    modifier: Modifier = Modifier,
    textLines: List<String>,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    bulletColor: Color = MaterialTheme.colorScheme.primary,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(25.dp)
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        textLines.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alignBy { it.measuredHeight }
                        .border(2.dp, bulletColor, CircleShape)
                )

                Text(
                    text = item,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignBy(FirstBaseline)
                )
            }
        }
    }
}