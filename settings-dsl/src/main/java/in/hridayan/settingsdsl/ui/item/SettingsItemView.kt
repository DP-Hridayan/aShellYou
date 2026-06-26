@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.settingsdsl.ui.item

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.model.ButtonGroupOption
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.settingsdsl.model.SettingsItem
import `in`.hridayan.settingsdsl.ui.card.CustomCard
import `in`.hridayan.settingsdsl.ui.card.CustomCardShape
import `in`.hridayan.settingsdsl.ui.card.cardShapeForPosition

/**
 * Renders a single [SettingsItem] matching the existing app item UI exactly:
 * - Switches: check/close thumb icon
 * - SwitchBanner: always primaryContainer, rounded pill, headlineSmall
 * - RadioGroup: surfaceContainer cards with RadioButton
 * - ButtonGroup: Material3 connected ToggleButtons
 * - Clickable: surfaceContainer card, highlighted when [SettingsItem.isHighlighted]
 *
 * @param hapticsEnabled Whether haptic feedback fires on all interactions.
 */
@Composable
fun SettingsItemView(
    item: SettingsItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    isChecked: Boolean = false,
    selectedValue: Int = -1,
    onClick: () -> Unit = {},
    onToggle: () -> Unit = {},
    onValueChange: (Int) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    fun hapticClick() {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.Companion.ContextClick)
    }

    fun hapticToggle() {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.Companion.ToggleOn)
    }

    when (item.behavior) {
        is ItemBehavior.Switch -> SwitchItemView(
            modifier = modifier,
            item = item,
            enabled = enabled,
            isChecked = isChecked,
            onToggle = { hapticToggle(); onToggle() },
        )

        is ItemBehavior.SwitchBanner -> SwitchBannerItemView(
            modifier = modifier,
            item = item,
            enabled = enabled,
            isChecked = isChecked,
            onToggle = { hapticToggle(); onToggle() },
        )

        is ItemBehavior.Clickable -> ClickableItemView(
            modifier = modifier,
            item = item,
            enabled = enabled,
            onClick = { hapticClick(); onClick() },
        )

        is ItemBehavior.RadioGroup -> RadioGroupItemView(
            modifier = modifier,
            options = item.behavior.options,
            selectedValue = selectedValue,
            onSelect = { v -> hapticToggle(); onValueChange(v) },
        )

        is ItemBehavior.ButtonGroup -> ButtonGroupItemView(
            modifier = modifier,
            options = item.behavior.options,
            selectedValue = selectedValue,
            onSelect = { v -> hapticClick(); onValueChange(v) },
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
private fun ItemLeadingIcon(item: SettingsItem) {
    when {
        item.icon != null -> Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        item.iconResId != null -> Icon(
            painter = painterResource(item.iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun SettingsItem.hasIcon() = icon != null || iconResId != null

@Composable
private fun ClickableItemView(
    modifier: Modifier,
    item: SettingsItem,
    enabled: Boolean,
    onClick: () -> Unit
) {
    CustomCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = item.shape,
        colors = highlightCardColors(item.isHighlighted),
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
            if (item.hasIcon()) ItemLeadingIcon(item)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (item.title.isNotEmpty()) Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                if (item.description.isNotEmpty()) Text(
                    text = item.description,
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
    item: SettingsItem,
    enabled: Boolean,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    CustomCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        shape = item.shape,
        colors = highlightCardColors(item.isHighlighted),
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
            if (item.hasIcon()) ItemLeadingIcon(item)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (item.title.isNotEmpty()) Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                if (item.description.isNotEmpty()) Text(
                    text = item.description,
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
    item: SettingsItem,
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
                text = item.title,
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
