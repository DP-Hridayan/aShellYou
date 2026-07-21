package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic

@Composable
fun FastbootQuickToolsCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    title: String,
    painter: Painter,
    colors: CardColors = CardDefaults.cardColors(),
    enabled: Boolean = true
) {
    val targetedColors = if (enabled) colors else CardDefaults.cardColors(
        containerColor = colors.disabledContainerColor,
        contentColor = colors.disabledContentColor
    )

    CustomCard(
        modifier = modifier,
        colors = targetedColors,
        clickable = enabled,
        onClick = withHaptic {
            onClick()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painter,
                contentDescription = null
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized
            )
        }
    }
}
