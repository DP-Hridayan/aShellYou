@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@Composable
fun CollapsibleCard(
    modifier: Modifier = Modifier,
    collapsedContent: @Composable (modifier: Modifier) -> Unit,
    expandedContent: @Composable () -> Unit,
    onStateChanged: ((Boolean) -> Unit)? = null,
    enableBorder: Boolean = true
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.largeIncreased)
            .clickable(enabled = true) {
                expanded = !expanded
                onStateChanged?.invoke(expanded)
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.largeIncreased
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.largeIncreased
    ) {
        Column(
            Modifier
                .padding(Dimens.paddingMedium)
                .animateContentSize(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
            ) {

                collapsedContent(Modifier.weight(1f))

                val rotateAngle by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = "Expand",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .rotate(rotateAngle)
                )
            }

            if (expanded) expandedContent()
        }
    }
}