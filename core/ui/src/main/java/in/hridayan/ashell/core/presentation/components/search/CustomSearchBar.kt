package `in`.hridayan.ashell.core.presentation.components.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

/**
 * A customizable search bar composable that allows users to input and filter text with optional
 * leading and trailing icons. This overload is a convenience for simple [String] based state.
 *
 * @param modifier The [Modifier] to be applied to the search bar layout.
 * @param value The current text value of the search bar.
 * @param onValueChange A callback invoked whenever the text input changes.
 * @param leadingIcon A composable function that displays an icon or UI element before the text field.
 * Defaults to a search icon.
 * @param trailingIcon A composable function that displays icons or UI elements after the text field.
 * @param hint The placeholder text shown when the search input is empty. Defaults to "Search...".
 * @param keyboardOptions Software keyboard options like IME action and keyboard type.
 * @param shape The shape of the search bar container.
 * @param colors The colors for the container and its content.
 * @param singleLine When true, this text field becomes a single horizontally scrolling line
 * instead of wrapping onto multiple lines.
 * @param maxLines The maximum height in terms of maximum number of visible lines.
 */
@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable () -> Unit = {
        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
            )
        }
    },
    trailingIcon: @Composable RowScope.() -> Unit = {},
    hint: String = "Search...",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shape: CustomCardShape = CustomCardShape(50),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    CustomSearchBar(
        modifier = modifier,
        value = TextFieldValue(text = value, selection = TextRange(value.length)),
        onValueChange = { onValueChange(it.text) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        hint = hint,
        keyboardOptions = keyboardOptions,
        shape = shape,
        colors = colors,
        singleLine = singleLine,
        maxLines = maxLines
    )
}

/**
 * A customizable search bar composable that allows users to input and filter text with optional
 * leading and trailing icons. This overload uses [TextFieldValue] for more advanced control
 * over cursor and selection.
 *
 * @param modifier The [Modifier] to be applied to the search bar layout.
 * @param value The current [TextFieldValue] of the search bar.
 * @param onValueChange A callback invoked whenever the text or selection changes.
 * @param leadingIcon A composable function that displays an icon or UI element before the text field.
 * Defaults to a search icon.
 * @param trailingIcon A composable function that displays icons or UI elements after the text field.
 * @param hint The placeholder text shown when the search input is empty. Defaults to "Search...".
 * @param keyboardOptions Software keyboard options like IME action and keyboard type.
 * @param shape The shape of the search bar container.
 * @param colors The colors for the container and its content.
 * @param singleLine When true, this text field becomes a single horizontally scrolling line
 * instead of wrapping onto multiple lines.
 * @param maxLines The maximum height in terms of maximum number of visible lines.
 */
@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    leadingIcon: @Composable () -> Unit = {
        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
            )
        }
    },
    trailingIcon: @Composable RowScope.() -> Unit = {},
    hint: String = "Search...",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shape: CustomCardShape = CustomCardShape(50),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    CustomCard(
        modifier = modifier,
        shape = shape,
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .animateContentSize(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()

            TextField(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = hint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.contentColor
                    )
                },
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = colors.contentColor,
                    unfocusedTextColor = colors.contentColor,
                    disabledTextColor = colors.contentColor
                ),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                trailingIcon()
            }
        }
    }
}
