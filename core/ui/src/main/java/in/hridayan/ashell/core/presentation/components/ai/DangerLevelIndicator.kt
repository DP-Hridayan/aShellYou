@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.ai.DangerLevel
import `in`.hridayan.ashell.core.resources.R

private data class DangerLevelIndicatorObject(
    val containerColor: Color,
    val contentColor: Color,
    val label: String,
    val icon: ImageVector
)

/**
 * A styled badge/indicator for the danger level of a command.
 * Each level has distinct colors and an icon for immediate visual recognition.
 */
@Composable
fun DangerLevelIndicator(
    dangerLevel: DangerLevel,
    modifier: Modifier = Modifier
) {
    val dangerLevelObject = dangerLevelObject(dangerLevel)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color = dangerLevelObject.containerColor)
            .border(
                width = 1.dp,
                color = dangerLevelObject.contentColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = dangerLevelObject.icon,
            tint = dangerLevelObject.contentColor,
            contentDescription = null
        )

        Text(
            text = dangerLevelObject.label,
            color = dangerLevelObject.contentColor,
            style = MaterialTheme.typography.labelLargeEmphasized
        )
    }
}

@Composable
private fun dangerLevelObject(dangerLevel: DangerLevel): DangerLevelIndicatorObject {
    val dangerLevelObject = when (dangerLevel) {
        DangerLevel.SAFE -> DangerLevelIndicatorObject(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            label = stringResource(R.string.safe),
            icon = ImageVector.vectorResource(R.drawable.ic_verified_user)
        )


        DangerLevel.LOW_RISK -> DangerLevelIndicatorObject(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            label = stringResource(R.string.low_risk),
            icon = ImageVector.vectorResource(R.drawable.ic_shield)
        )

        DangerLevel.MODERATE -> DangerLevelIndicatorObject(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            label = stringResource(R.string.moderate),
            icon = ImageVector.vectorResource(R.drawable.ic_gpp_maybe)
        )

        DangerLevel.DANGEROUS -> DangerLevelIndicatorObject(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            label = stringResource(R.string.dangerous),
            icon = ImageVector.vectorResource(R.drawable.ic_dangerous)
        )

        DangerLevel.CRITICAL -> DangerLevelIndicatorObject(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            label = stringResource(R.string.critical),
            icon = ImageVector.vectorResource(R.drawable.ic_crisis_alert)
        )
    }

    return dangerLevelObject
}
