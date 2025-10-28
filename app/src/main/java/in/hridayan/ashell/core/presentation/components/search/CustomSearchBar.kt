package `in`.hridayan.ashell.core.presentation.components.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard

@Composable
fun CustomSearchBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "Search...",
    onClearClick: () -> Unit = {},
    showDismissButton: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current
    val focusManager = LocalFocusManager.current
    val isHintVisible = value.text.isEmpty()

    PillShapedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }

            if (value.text.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = "Clear text",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickable(
                            enabled = true,
                            interactionSource = null,
                            onClick = {
                                weakHaptic()
                                onClearClick()
                                focusManager.clearFocus()
                            }
                        )
                )
            }

            if (showDismissButton) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cross),
                    contentDescription = "Clear text",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickable(
                            enabled = true,
                            interactionSource = null,
                            onClick = {
                                weakHaptic()
                                onDismiss()
                                onClearClick()
                                focusManager.clearFocus()
                            }
                        )
                )
            }
        }
    }
}
