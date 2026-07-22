package `in`.hridayan.ashell.shell.fastboot.presentation.components.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R

@Composable
fun UnlockStatusCard(
    modifier: Modifier = Modifier,
    isUnlocked: Boolean?
) {
    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        clickable = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoResizeableText(
                modifier = Modifier.alpha(0.9f),
                text = stringResource(R.string.unlock_status),
                style = MaterialTheme.typography.titleMediumEmphasized
            )

            if (isUnlocked == null) {
                Text(
                    text = stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.run {
                            if (isUnlocked) error else primary
                        }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = if (isUnlocked) painterResource(R.drawable.ic_lock_open)
                        else painterResource(R.drawable.ic_lock),
                        tint = MaterialTheme.colorScheme.run { if (isUnlocked) onError else onPrimary },
                        contentDescription = null
                    )

                    Text(
                        text = if (isUnlocked) stringResource(R.string.unlocked)
                        else stringResource(R.string.locked),
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                        color = MaterialTheme.colorScheme.run {
                            if (isUnlocked) onError else onPrimary
                        }
                    )
                }
            }
        }
    }
}
