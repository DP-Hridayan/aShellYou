package `in`.hridayan.settingsdsl.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.ResolvedGroup
import `in`.hridayan.settingsdsl.model.SettingsItem
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.resolver.resolveAll

/**
 * Renders a list of [ResolvedGroup]s into a [LazyListScope].
 *
 * Use this overload when manual control over state and events is required.
 *
 * @param groups The resolved groups to render.
 * @param modifier Modifier applied to each item container.
 * @param itemPaddingHorizontal Horizontal padding for each item.
 * @param itemPaddingVertical Vertical padding for each item.
 * @param hapticsEnabled Whether haptic feedback is enabled for interactions.
 * @param isChecked Provider for the checked state of switch items.
 * @param selectedValue Provider for the selected value of group items.
 * @param onItemClick Callback for item clicks.
 * @param onBooleanToggle Callback for boolean state toggles.
 * @param onIntChanged Callback for integer value changes.
 * @param customSlotContent Content to render for custom slots.
 * @param categoryHeader Optional override for the category header UI.
 */
fun LazyListScope.settingsContent(
    groups: List<ResolvedGroup>,
    modifier: Modifier = Modifier,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    hapticsEnabled: Boolean = true,
    isChecked: (SettingsKey<*>) -> Boolean = { false },
    selectedValue: (SettingsKey<*>) -> Int = { -1 },
    onItemClick: (SettingsKey<*>) -> Unit = {},
    onBooleanToggle: (SettingsKey<*>) -> Unit = {},
    onIntChanged: (SettingsKey<*>, Int) -> Unit = { _, _ -> },
    customSlotContent: @Composable (CustomSlot) -> Unit = {},
    categoryHeader: (@Composable (String) -> Unit)? = null,
) {
    groups.forEach { group ->
        when (group) {
            is ResolvedGroup.Divider -> item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            is ResolvedGroup.Custom -> item(key = group.slot.id) {
                customSlotContent(group.slot)
            }

            is ResolvedGroup.ItemGroup -> {
                // Category header
                group.categoryTitle?.let { title ->
                    item(key = "category_$title") {
                        if (categoryHeader != null) {
                            categoryHeader(title)
                        } else {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 30.dp,
                                        bottom = 10.dp,
                                    )
                                    .animateItem(),
                            )
                        }
                    }
                }

                // Items
                itemsIndexed(
                    items = group.items,
                    key = { _, item -> item.key.name },
                ) { _, item ->
                    SettingsItemEntry(
                        item = item,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(
                                horizontal = itemPaddingHorizontal,
                                vertical = itemPaddingVertical
                            )
                            .animateItem()
                            .then(modifier),
                        hapticsEnabled = hapticsEnabled,
                        isChecked = isChecked(item.key),
                        selectedValue = selectedValue(item.key),
                        onItemClick = onItemClick,
                        onBooleanToggle = onBooleanToggle,
                        onIntChanged = onIntChanged,
                    )
                }
            }
        }
    }
}

/**
 * Internal entry point for a single settings item, handling memoization of event callbacks.
 */
@Composable
private fun SettingsItemEntry(
    item: SettingsItem,
    modifier: Modifier,
    hapticsEnabled: Boolean,
    isChecked: Boolean,
    selectedValue: Int,
    onItemClick: (SettingsKey<*>) -> Unit,
    onBooleanToggle: (SettingsKey<*>) -> Unit,
    onIntChanged: (SettingsKey<*>, Int) -> Unit,
) {
    val key = item.key
    val onClick = remember(key) { { onItemClick(key) } }
    val onToggle = remember(key) { { onBooleanToggle(key) } }
    val onValueChange = remember(key) { { v: Int -> onIntChanged(key, v) } }

    SettingsItemView(
        modifier = modifier,
        title = item.title,
        description = item.description,
        icon = item.icon,
        iconResId = item.iconResId,
        shape = item.shape,
        isHighlighted = item.isHighlighted,
        enableExperimentalFlag = item.enableExperimentalFlag,
        experimentalFlagText = item.experimentalFlagText,
        behavior = item.behavior,
        enabled = item.enabled,
        hapticsEnabled = hapticsEnabled,
        isChecked = isChecked,
        selectedValue = selectedValue,
        onClick = onClick,
        onToggle = onToggle,
        onValueChange = onValueChange,
    )
}

