@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.navigation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun FloatingNavPill(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    colors: FloatingNavPillColors = FloatingNavPillDefaults.colors(),
    items: List<FloatingNavPillItem> = FloatingNavPillDefaults.items(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    animationSpec: AnimationSpec<Dp> = MaterialTheme.motionScheme.fastSpatialSpec()
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = colors.floatingContainerColor,
            contentColor = colors.floatingContentColor
        ),
        elevation = elevation
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / items.size
            val offsetX by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = animationSpec,
                label = "pill_offset"
            )

            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.selectedContainerColor)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectionChange(index) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizeableText(
                            text = item.text,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.run {
                                if (index == selectedIndex) selectedContentColor
                                else floatingContentColor
                            }
                        )
                    }
                }
            }
        }
    }
}

object FloatingNavPillDefaults {
    @Composable
    fun colors(
        selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        floatingContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
        floatingContentColor: Color = MaterialTheme.colorScheme.onSurface
    ) = FloatingNavPillColors(
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
        floatingContainerColor = floatingContainerColor,
        floatingContentColor = floatingContentColor
    )

    fun items(items: List<FloatingNavPillItem> = emptyList()) = items
}

data class FloatingNavPillColors(
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val floatingContainerColor: Color,
    val floatingContentColor: Color
)

data class FloatingNavPillItem(
    val text: String
)

