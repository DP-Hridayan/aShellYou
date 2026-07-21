package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.ui.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun ActiveSlotsCard(
    modifier: Modifier = Modifier,
    activeSlotIsA: Boolean = false,
    activeSlotIsB: Boolean = false
) {
    val activeSlotContainerColor = MaterialTheme.colorScheme.primary
    val activeSlotContentColor = MaterialTheme.colorScheme.onPrimary
    val inActiveSlotContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val inActiveSlotContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        clickable = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizeableText(
                modifier = Modifier.alpha(0.9f),
                text = stringResource(R.string.active_slot),
                style = MaterialTheme.typography.titleMediumEmphasized
            )

            if (activeSlotIsA || activeSlotIsB) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeSlotIsA) activeSlotContainerColor else inActiveSlotContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                            text = "A",
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlotIsA) activeSlotContentColor else inActiveSlotContentColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeSlotIsB) activeSlotContainerColor else inActiveSlotContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                            text = "B",
                            style = MaterialTheme.typography.displaySmallEmphasized,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSlotIsB) activeSlotContentColor else inActiveSlotContentColor
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
