package `in`.hridayan.settingsdsl.ui.item

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.controller.SettingsController
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.ResolvedGroup
import `in`.hridayan.settingsdsl.model.SettingsKey

/**
 * Renders a list of [ResolvedGroup]s into a [LazyListScope] using a [SettingsController]
 * for all state reading and event handling.
 *
 * This is the **recommended** API — it eliminates per-screen callback boilerplate.
 *
 * @param groups                The resolved groups to render.
 * @param controller            Provides checked/selected state and handles click/toggle/change events.
 * @param modifier              Modifier applied to each item card.
 * @param itemPaddingHorizontal Horizontal padding applied to each item.
 * @param itemPaddingVertical   Vertical padding applied to each item.
 * @param hapticsEnabled        Whether haptic feedback fires on taps, toggles, and value changes.
 * @param customSlotContent     Composable content to render for each [ResolvedGroup.Custom] slot.
 * @param categoryHeader        Optional override for rendering category header text.
 */
fun LazyListScope.settingsContent(
    groups: List<ResolvedGroup>,
    controller: SettingsController,
    modifier: Modifier = Modifier,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    hapticsEnabled: Boolean = true,
    customSlotContent: @Composable (CustomSlot) -> Unit = {},
    categoryHeader: (@Composable (String) -> Unit)? = null,
) {
    settingsContent(
        groups = groups,
        modifier = modifier,
        itemPaddingHorizontal = itemPaddingHorizontal,
        itemPaddingVertical = itemPaddingVertical,
        hapticsEnabled = hapticsEnabled,
        isChecked = controller::isChecked,
        selectedValue = controller::selectedValue,
        onItemClick = controller::onItemClick,
        onBooleanToggle = controller::onBooleanToggle,
        onIntChanged = controller::onIntChanged,
        customSlotContent = customSlotContent,
        categoryHeader = categoryHeader,
    )
}

/**
 * Renders a list of [ResolvedGroup]s into a [LazyListScope].
 *
 * Call this inside a `LazyColumn` block after obtaining groups from [SettingsPage.resolveAll].
 *
 * @param groups                The resolved groups to render.
 * @param modifier              Modifier applied to each item card.
 * @param itemPaddingHorizontal Horizontal padding applied to each item.
 * @param itemPaddingVertical   Vertical padding applied to each item.
 * @param hapticsEnabled        Whether haptic feedback fires on taps, toggles, and value changes.
 *                              Defaults to `true`. Pass `false` to suppress all haptics.
 * @param isChecked             Provides the checked state for [ItemBehavior.Switch] and [ItemBehavior.SwitchBanner].
 * @param selectedValue         Provides the selected int value for [ItemBehavior.RadioGroup] and [ItemBehavior.ButtonGroup].
 * @param onItemClick           Called when a [ItemBehavior.Clickable] item is tapped.
 * @param onBooleanToggle       Called when a [ItemBehavior.Switch] or [ItemBehavior.SwitchBanner] is toggled.
 * @param onIntChanged          Called when a [ItemBehavior.RadioGroup] or [ItemBehavior.ButtonGroup] selection changes.
 * @param customSlotContent     Composable content to render for each [ResolvedGroup.Custom] slot.
 * @param categoryHeader        Optional override for rendering category header text.
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
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 30.dp,
                                    bottom = 10.dp,
                                ),
                            )
                        }
                    }
                }

                // Items
                itemsIndexed(
                    items = group.items,
                    key = { _, item -> item.key.name },
                ) { _, item ->
                    SettingsItemView(
                        item = item,
                        modifier = modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = itemPaddingHorizontal, vertical = itemPaddingVertical)
                            .animateItem(),
                        hapticsEnabled = hapticsEnabled,
                        isChecked = isChecked(item.key),
                        selectedValue = selectedValue(item.key),
                        onClick = { onItemClick(item.key) },
                        onToggle = { onBooleanToggle(item.key) },
                        onValueChange = { value -> onIntChanged(item.key, value) },
                    )
                }
            }
        }
    }
}
