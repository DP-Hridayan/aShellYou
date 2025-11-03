package `in`.hridayan.ashell.core.presentation.components.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard

/**
 * A customizable search bar composable that allows users to input and filter text with optional
 * leading and trailing icons.
 *
 * This composable is flexible enough to support different use cases, such as showing a search
 * icon on the left, a clear button on the right, and a hint when the input is empty.
 *
 * @param modifier The [Modifier] to be applied to the search bar layout, allowing for layout
 * customization such as padding, width, or background.
 *
 * @param value The current text state of the search bar represented as a [TextFieldValue].
 * This state contains both the text and cursor/selection information.
 *
 * @param onValueChange A callback invoked whenever the text input changes. Use this to update
 * your state and perform any filtering or search logic.
 *
 * @param leadingIcon A composable function that displays an icon or any other UI element
 * before the text field. By default, it shows a search icon.
 *
 * @param trailingIcon A composable function that displays an icon or UI element after the
 * text field, such as a clear or filter button. Defaults to an empty composable.
 *
 * @param hint The placeholder text shown when the search input is empty. Defaults to `"Search..."`.
 *
 * @param colors The search container color and content color.
 */
@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    leadingIcon: @Composable () -> Unit = {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
        )
    },
    trailingIcon: @Composable RowScope.() -> Unit = {},
    hint: String = "Search...",
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
) {
    val isHintVisible = value.text.isEmpty()

    PillShapedCard(
        modifier = modifier,
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = colors.contentColor
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                )

                if (isHintVisible) {
                    Text(
                        text = hint,
                        fontSize = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                trailingIcon()
            }
        }
    }
}
