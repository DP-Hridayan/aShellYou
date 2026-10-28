@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.switch.AppSwitch
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape

/**
 * Spacing of the tunnel editor, on Material's 8 dp grid and matching the rest of the app: content
 * sits 20 dp from the screen edge, section labels line up with the text inside cards, and rows
 * that belong together are grouped 2 dp apart.
 */
object EditorSpacing {
    val PageHorizontal = 20.dp
    val SectionTop = 24.dp
    val SectionLabelInset = 4.dp
    val SectionLabelBottom = 12.dp
    val GroupGap = 2.dp
    val BlockGap = 12.dp
    val CaptionInset = 20.dp
    val CaptionTop = 6.dp
    val CardHorizontal = 20.dp
    val CardVertical = 16.dp
    val FieldHorizontal = 20.dp
    val FieldVertical = 12.dp
    val SwitchRowVertical = 8.dp
    val ContentGap = 16.dp
    val BottomClearance = 120.dp
}

private val OuterCorner = 24.dp
private val InnerCorner = 4.dp

/**
 * Where a row sits in its group, which decides its corners: rounded on the outside of the group and
 * tight between rows. [Start] and [End] join two rows side by side.
 */
enum class GroupPosition(
    private val topStart: Dp,
    private val topEnd: Dp,
    private val bottomStart: Dp,
    private val bottomEnd: Dp
) {
    Single(OuterCorner, OuterCorner, OuterCorner, OuterCorner),
    First(OuterCorner, OuterCorner, InnerCorner, InnerCorner),
    Middle(InnerCorner, InnerCorner, InnerCorner, InnerCorner),
    Last(InnerCorner, InnerCorner, OuterCorner, OuterCorner),
    Start(OuterCorner, InnerCorner, OuterCorner, InnerCorner),
    End(InnerCorner, OuterCorner, InnerCorner, OuterCorner);

    /** The shape for a [CustomCard]. */
    val card: CustomCardShape
        get() = CustomCardShape(topStart = topStart, topEnd = topEnd, bottomStart = bottomStart, bottomEnd = bottomEnd)

    /** The same corners for a text field. */
    val field: Shape
        get() = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )

    companion object {
        fun of(index: Int, count: Int): GroupPosition = when {
            count == 1 -> Single
            index == 0 -> First
            index == count - 1 -> Last
            else -> Middle
        }
    }
}

/** The caption above a group of editor rows. */
@Composable
fun EditorSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(
            start = EditorSpacing.SectionLabelInset,
            top = EditorSpacing.SectionTop,
            bottom = EditorSpacing.SectionLabelBottom
        ),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

/** Rows that belong together, stacked [EditorSpacing.GroupGap] apart. */
@Composable
fun EditorGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EditorSpacing.GroupGap),
        content = content
    )
}

/**
 * Supporting text under a group: a hint, or an error in the error colour. It animates in and out
 * so the layout below does not jump.
 */
@Composable
fun EditorCaption(text: String?, isError: Boolean = false) {
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            modifier = Modifier.padding(start = EditorSpacing.CaptionInset, top = EditorSpacing.CaptionTop),
            text = text.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A borderless filled field in the app's card style. [position] places it in its group, so a run
 * of fields reads as one surface. Its text lines up with the text of the cards around it, and the
 * floating label and value get more room than Material's default.
 */
@Composable
fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    position: GroupPosition,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    monospace: Boolean = false
) {
    val colors = editorFieldColors()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val textColor = when {
        isError -> colors.errorTextColor
        focused -> colors.focusedTextColor
        else -> colors.unfocusedTextColor
    }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = textColor,
        fontFamily = if (monospace) FontFamily.Monospace else null
    )
    // The material TextField overload for a String value takes no content padding, so the field is
    // built from its parts to give the label and value more room.
    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = TextFieldDefaults.MinHeight),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(if (isError) colors.errorCursorColor else colors.cursorColor),
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            isError = isError,
            label = { Text(label) },
            trailingIcon = trailingIcon,
            shape = position.field,
            colors = colors,
            contentPadding = TextFieldDefaults.contentPaddingWithLabel(
                start = EditorSpacing.FieldHorizontal,
                end = EditorSpacing.FieldHorizontal,
                top = EditorSpacing.FieldVertical,
                bottom = EditorSpacing.FieldVertical
            )
        )
    }
}

@Composable
private fun editorFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

/** A labelled switch row in the app's card style, placed in its group by [position]. */
@Composable
fun EditorSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    position: GroupPosition,
    description: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val toggle = { isChecked: Boolean ->
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
        onCheckedChange(isChecked)
    }

    CustomCard(
        modifier = Modifier.fillMaxWidth(),
        shape = position.card,
        onClick = { toggle(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorSpacing.CardHorizontal, vertical = EditorSpacing.SwitchRowVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EditorSpacing.ContentGap)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AppSwitch(checked = checked, onCheckedChange = toggle)
        }
    }
}
