package `in`.hridayan.ashell.settings.presentation.page.aimodels.components.modelmanager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape

/**
 * Visual storage bar showing AI model storage usage vs available device storage.
 */
@Composable
fun StorageUsageBar(
    modifier: Modifier = Modifier,
    usedBytes: Long,
    totalAvailableBytes: Long,
) {
    val usedMb = usedBytes / 1_048_576.0
    val fraction = if (totalAvailableBytes > 0) {
        (usedBytes.toFloat() / totalAvailableBytes).coerceIn(0f, 1f)
    } else 0f

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        shape = CustomCardShape(50)
    ) {
        Column(modifier = Modifier.padding(horizontal = 25.dp, vertical = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Text(
                text = stringResource(R.string.storage_used_by_ai_models, usedMb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
