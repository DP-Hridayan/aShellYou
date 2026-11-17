package `in`.hridayan.ashell.home.presentation.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.home.presentation.model.GuideItem

private val IconContainerSize = 42.dp
private val IconSize = 22.dp
private val TrailingIconSize = 18.dp
private val IconCornerRadius = 12.dp
private const val TrailingIconAlpha = 0.65f
private val CardHorizontalPadding = 16.dp
private val ContentHorizontalPadding = 16.dp
private val ContentVerticalPadding = 14.dp
private val IconTextSpacing = 14.dp
private val TextTrailingSpacing = 10.dp
private val TitleDescSpacing = 3.dp

@Composable
fun GuideItemCard(
    modifier: Modifier = Modifier,
    guide: GuideItem,
    shape: CustomCardShape,
    onClick: () -> Unit
) {
    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CardHorizontalPadding),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = withHaptic { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ContentHorizontalPadding, vertical = ContentVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(IconContainerSize)
                    .clip(RoundedCornerShape(IconCornerRadius))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    painter = painterResource(guide.iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(IconTextSpacing))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(guide.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(TitleDescSpacing))

                Text(
                    text = stringResource(guide.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(TextTrailingSpacing))

            Icon(
                modifier = Modifier.size(TrailingIconSize),
                painter = painterResource(R.drawable.ic_open_in_new),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TrailingIconAlpha)
            )
        }
    }
}
