@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.settingsdsl.ui.item

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.ui.card.CustomCard
import `in`.hridayan.settingsdsl.ui.card.CustomCardShape
import `in`.hridayan.settingsdsl.ui.card.cardShapeForPosition

/**
 * Renders a single settings item matching the app item UI:
 * - Switches: check/close thumb icon
 * - SwitchBanner: always primaryContainer, rounded pill, headlineSmall
 * - RadioGroup: surfaceContainer cards with RadioButton
 * - ButtonGroup: Material3 connected ToggleButtons
 * - Clickable: surfaceContainer card, highlighted when [isHighlighted]
 *
 * All parameters are stable primitives so Compose can skip recomposition
 * correctly — e.g. toggling [isChecked] won't re-draw the title or icon.
 *
 * @param hapticsEnabled Whether haptic feedback fires on all interactions.
 */
@Composable
fun SettingsItemView(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    icon: ImageVector? = null,
    @DrawableRes iconResId: Int? = null,
    shape: CustomCardShape = CustomCardShape(),
    isHighlighted: Boolean = false,
    enableExperimentalFlag: Boolean = false,
    experimentalFlagText: String = "Experimental",
    behavior: ItemBehavior,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    isChecked: Boolean = false,
    selectedValue: Int = -1,
    onClick: () -> Unit = {},
    onToggle: () -> Unit = {},
    onValueChange: (Int) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    val wrappedOnToggle = remember(onToggle, hapticsEnabled) {
        {
            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onToggle()
        }
    }
    val wrappedOnClick = remember(onClick, hapticsEnabled) {
        {
            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        }
    }
    val wrappedOnValueChange = remember(onValueChange, hapticsEnabled) {
        { v: Int ->
            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onValueChange(v)
        }
    }

    when (behavior) {
        is ItemBehavior.Switch -> SwitchItemView(
            modifier = modifier,
            title = title,
            description = description,
            icon = icon,
            iconResId = iconResId,
            shape = shape,
            isHighlighted = isHighlighted,
            enableExperimentalFlag = enableExperimentalFlag,
            experimentalFlagText = experimentalFlagText,
            enabled = enabled,
            isChecked = isChecked,
            onToggle = wrappedOnToggle,
        )

        is ItemBehavior.SwitchBanner -> SwitchBannerItemView(
            modifier = modifier,
            title = title,
            enabled = enabled,
            isChecked = isChecked,
            onToggle = wrappedOnToggle,
        )

        is ItemBehavior.Clickable -> ClickableItemView(
            modifier = modifier,
            title = title,
            description = description,
            icon = icon,
            iconResId = iconResId,
            shape = shape,
            isHighlighted = isHighlighted,
            enabled = enabled,
            onClick = wrappedOnClick,
        )

        is ItemBehavior.RadioGroup -> RadioGroupItemView(
            modifier = modifier,
            options = behavior.options,
            selectedValue = selectedValue,
            onSelect = wrappedOnValueChange,
        )

        is ItemBehavior.ButtonGroup -> ButtonGroupItemView(
            modifier = modifier,
            options = behavior.options,
            selectedValue = selectedValue,
            onSelect = wrappedOnValueChange,
        )
    }
}

@Composable
private fun highlightCardColors(isHighlighted: Boolean): CardColors {
    val normal = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurface = MaterialTheme.colorScheme.onSurface

    if (!isHighlighted) {
        return CardDefaults.cardColors(
            containerColor = normal,
            contentColor = onSurface,
        )
    }

    // Finite blink: 3 pulses (~2.4 s total), then stays at normal color.
    // Using Animatable instead of rememberInfiniteTransition means the animation
    // ends without mutating any external state (isHighlighted stays true, resolveAll
    // never recomposes, and animateItem() on the LazyColumn is never triggered).
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        repeat(3) {
            animatable.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
            animatable.animateTo(0f, animationSpec = tween(400, easing = LinearEasing))
        }
        // stays at 0f (normal color) — no more recomposition needed
    }

    return CardDefaults.cardColors(
        containerColor = lerp(normal, highlight, animatable.value),
        contentColor = onSurface,
    )
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(),
        thumbContent = {
            Icon(
                imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        },
    )
}

@Composable
private fun ItemLeadingIcon(icon: ImageVector?, @DrawableRes iconResId: Int?) {
    when {
        icon != null -> Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        iconResId != null -> Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExperimentalBadge(
    modifier: Modifier = Modifier,
    label: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ClickableItemView(
    modifier: Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    @DrawableRes iconResId: Int?,
    shape: CustomCardShape,
    isHighlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    CustomCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = shape,
        colors = highlightCardColors(isHighlighted),
        clickable = enabled,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            ItemLeadingIcon(icon = icon, iconResId = iconResId)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (title.isNotEmpty()) Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                if (description.isNotEmpty()) Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }
    }
}

@Composable
private fun SwitchItemView(
    modifier: Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    @DrawableRes iconResId: Int?,
    shape: CustomCardShape,
    isHighlighted: Boolean,
    enableExperimentalFlag: Boolean,
    experimentalFlagText: String,
    enabled: Boolean,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    CustomCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = shape,
        colors = highlightCardColors(isHighlighted),
        clickable = enabled,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            ItemLeadingIcon(icon = icon, iconResId = iconResId)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    if (title.isNotEmpty()) Text(
                        modifier = Modifier.weight(weight = 1f, fill = false),
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )

                    if (enableExperimentalFlag) ExperimentalBadge(label = experimentalFlagText)
                }
                if (description.isNotEmpty()) Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7f)
                )
            }
            // Switch also fires haptic via onToggle which already wraps the haptic call
            SettingsSwitch(
                checked = isChecked,
                enabled = enabled,
                onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun SwitchBannerItemView(
    modifier: Modifier,
    title: String,
    enabled: Boolean,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    CustomCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = CustomCardShape(all = 50.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.run {
                if (enabled) primaryContainer else surfaceContainer
            },
            contentColor = MaterialTheme.colorScheme.run {
                if (enabled) onPrimaryContainer else onSurfaceVariant
            },
        ),
        clickable = enabled,
        onClick = onToggle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .basicMarquee(initialDelayMillis = 2500, repeatDelayMillis = 3000)
            )
            SettingsSwitch(
                checked = isChecked,
                enabled = enabled,
                onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun RadioGroupItemView(
    modifier: Modifier,
    options: List<RadioButtonOption>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = modifier) {
        options.forEachIndexed { i, option ->
            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                shape = cardShapeForPosition(i, options.size),
                onClick = { onSelect(option.value) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(option.labelResId),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    RadioButton(
                        selected = option.value == selectedValue,
                        onClick = { onSelect(option.value) })
                }
            }
        }
    }
}

@Composable
private fun ButtonGroupItemView(
    modifier: Modifier,
    options: List<ButtonGroupOption>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option.value == selectedValue,
                onCheckedChange = { onSelect(option.value) },
                modifier = Modifier.weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                option.iconResId?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null
                    )
                }
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(text = stringResource(option.labelResId))
            }
        }
    }
}
