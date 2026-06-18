@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.buttongroup

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

/**
 * A reusable wrapper around the new [ButtonGroup] API with overflow indicator support.
 *
 * Each [ButtonGroupItem] describes the button type, colors, text, icon, and click handler.
 * The composable renders each item via [ButtonGroupItem] for inline
 * display, and as a [DropdownMenuItem] when the item overflows into the overflow menu.
 *
 * @param items the list of button group items to render
 * @param modifier modifier for the underlying [ButtonGroup]
 * @param horizontalArrangement horizontal arrangement of the buttons
 */
@Composable
fun OverflowButtonGroup(
    items: List<ButtonGroupItem>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(15.dp),
) {
    val interactionSources = remember(items.size) {
        List(items.size) { MutableInteractionSource() }
    }

    ButtonGroup(
        modifier = modifier.fillMaxWidth(),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        items.forEachIndexed { index, item ->
            val isIconButton = item.buttonConfig.type == ButtonType.IconButton

            customItem(
                buttonGroupContent = {
                    ButtonGroupItem(
                        modifier = if (isIconButton) {
                            Modifier.animateWidth(interactionSources[index])
                        } else {
                            Modifier
                                .weight(1f)
                                .animateWidth(interactionSources[index])
                        },
                        buttonGroupItem = item,
                        interactionSource = interactionSources[index]
                    )
                },
                menuContent = {
                    DropdownMenuItem(
                        enabled = item.enabled,
                        leadingIcon = {
                            when {
                                item.icon != null -> Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription
                                )

                                item.iconResId != null -> Icon(
                                    painter = painterResource(item.iconResId),
                                    contentDescription = item.contentDescription
                                )
                            }
                        },
                        text = {
                            if (item.text.isNotEmpty()) {
                                AutoResizeableText(text = item.text)
                            }
                        },
                        onClick = item.onClick
                    )
                }
            )
        }
    }
}
