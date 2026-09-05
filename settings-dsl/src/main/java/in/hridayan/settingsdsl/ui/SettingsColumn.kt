package `in`.hridayan.settingsdsl.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.dsl.SettingsGraphBuilder
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.SettingsGraphGroup
import `in`.hridayan.settingsdsl.ui.card.CustomCardShape
import `in`.hridayan.settingsdsl.ui.card.cardShapeForPosition
import `in`.hridayan.settingsdsl.ui.item.SettingsItemView
import kotlinx.coroutines.delay

private val NoOpAnyCallback: (Any) -> Unit = {}
private val NoOpAnyIntCallback: (Any, Int) -> Unit = { _, _ -> }
private val AlwaysFalse: (Any) -> Boolean = { false }
private val AlwaysNegativeOne: (Any) -> Int = { -1 }

/**
 * Renders a settings screen defined by the [content] DSL block.
 *
 * Callbacks are resolved in two tiers for each item:
 * 1. Per-item override declared inside the item builder block (highest priority).
 * 2. Global defaults from [LocalSettingsDslState] (lowest priority).
 *
 * The graph is rebuilt on every recomposition so that [visible] lambdas and dynamic title/
 * description lambdas always reflect the latest captured values from the enclosing composable.
 *
 * @param modifier Modifier applied to the [LazyColumn].
 * @param topAppBarState When provided, the top bar is collapsed before scrolling to a highlighted item.
 * @param hapticsEnabled Whether haptic feedback is enabled on item interactions.
 * @param itemPaddingHorizontal Horizontal padding applied to each item card.
 * @param itemPaddingVertical Vertical padding applied to each item card.
 * @param customSlotContent Composable invoked for [in.hridayan.settingsdsl.model.SettingsGraphGroup.Custom]
 *                          entries. Receives the [CustomSlot] identifier.
 * @param groupHeader Optional composable used to render group header labels. When null, a default
 *                    [Text] styled with [MaterialTheme.typography.labelLarge] is used.
 * @param listState The [LazyListState] controlling scroll position.
 * @param contentPadding Content padding applied to the [LazyColumn].
 * @param content DSL block defining the groups and items in this settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsColumn(
    modifier: Modifier = Modifier,
    topAppBarState: TopAppBarState? = null,
    hapticsEnabled: Boolean = true,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    customSlotContent: @Composable (CustomSlot) -> Unit = {},
    groupHeader: (@Composable (String) -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: SettingsGraphBuilder.() -> Unit,
) {
    val graph = SettingsGraphBuilder().apply(content).build()
    val dslState = LocalSettingsDslState.current
    val highlightState = dslState.highlightState
    val globalDefaults = dslState.onClickDefaults

    var totalIndex = 0
    var targetIndex = -1

    graph.groups.forEach { group ->
        when (group) {
            is SettingsGraphGroup.Group -> {
                val visibleNodes = group.nodes.filter { it.isVisible() }
                if (visibleNodes.isNotEmpty()) {
                    val hasHeader = group.titleResId != null || group.titleString.isNotEmpty()
                    if (hasHeader) totalIndex++
                    visibleNodes.forEach { node ->
                        if (node.key == highlightState.activeKey) targetIndex = totalIndex
                        totalIndex++
                    }
                }
            }

            is SettingsGraphGroup.Custom -> totalIndex++
            is SettingsGraphGroup.Divider -> totalIndex++
            is SettingsGraphGroup.RawItem -> totalIndex++
        }
    }

    val hasScrolled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(highlightState.activeKey, targetIndex) {
        if (highlightState.activeKey != null && targetIndex >= 0 && !hasScrolled.value) {
            delay(400)
            topAppBarState?.heightOffset = topAppBarState?.heightOffsetLimit ?: 0f
            listState.animateScrollToItem(targetIndex)
            highlightState.clear()
            hasScrolled.value = true
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
    ) {
        graph.groups.forEachIndexed { groupIndex, group ->
            when (group) {
                is SettingsGraphGroup.Group -> {
                    val visibleNodes = group.nodes.filter { it.isVisible() }
                    if (visibleNodes.isNotEmpty()) {
                        val headerKey = group.titleResId?.toString() ?: group.titleString

                        if (group.titleResId != null || group.titleString.isNotEmpty()) {
                            item(key = "header_$headerKey") {
                                val headerText = if (group.titleResId != null) {
                                    stringResource(group.titleResId)
                                } else {
                                    group.titleString
                                }
                                Box(modifier = Modifier.animateItem()) {
                                    if (groupHeader != null) {
                                        groupHeader(headerText)
                                    } else {
                                        Text(
                                            text = headerText,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(
                                                start = itemPaddingHorizontal + 16.dp,
                                                top = 24.dp,
                                                bottom = 8.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        itemsIndexed(
                            items = visibleNodes,
                            key = { _, node -> node.keyName }
                        ) { index, node ->
                            val title = node.dynamicTitle?.let { it() }
                                ?: node.staticTitleRes?.let { stringResource(it) }
                                ?: node.staticTitleString

                            val desc = node.dynamicDescription?.let { it() }
                                ?: node.staticDescRes?.let { stringResource(it) }
                                ?: node.staticDescString

                            val expFlag = node.experimentalFlagTextRes?.let { stringResource(it) }
                                ?: node.experimentalFlagTextString

                            val resolvedIsChecked = when (node.behavior) {
                                is ItemBehavior.Switch,
                                is ItemBehavior.SwitchBanner -> (node.isCheckedOverride
                                    ?: globalDefaults.isChecked ?: AlwaysFalse)(node.key)

                                else -> false
                            }
                            val resolvedSelectedVal = when (node.behavior) {
                                is ItemBehavior.RadioGroup,
                                is ItemBehavior.ButtonGroup -> (node.selectedValueOverride
                                    ?: globalDefaults.selectedValue ?: AlwaysNegativeOne)(node.key)

                                else -> -1
                            }

                            SettingsItemEntry(
                                nodeKey = node.key,
                                title = title,
                                description = desc,
                                iconVector = node.iconVector,
                                iconResId = node.iconResId,
                                shape = cardShapeForPosition(index, visibleNodes.size),
                                isHighlighted = node.key == highlightState.activeKey,
                                experimentalFlagText = expFlag,
                                behavior = node.behavior,
                                enabled = node.enabled,
                                hapticsEnabled = hapticsEnabled,
                                isChecked = resolvedIsChecked,
                                selectedValue = resolvedSelectedVal,
                                itemPaddingHorizontal = itemPaddingHorizontal,
                                itemPaddingVertical = itemPaddingVertical,
                                onClick = node.onClickOverride ?: NoOpAnyCallback,
                                onToggle = node.onToggleOverride ?: globalDefaults.onSwitchItem
                                ?: NoOpAnyCallback,
                                onIntChanged = node.onIntChangedOverride
                                    ?: globalDefaults.onIntChanged ?: NoOpAnyIntCallback,
                            )
                        }
                    }
                }

                is SettingsGraphGroup.Custom -> {
                    item(key = group.slot.id) {
                        Box(modifier = Modifier.animateItem()) {
                            customSlotContent(group.slot)
                        }
                    }
                }

                is SettingsGraphGroup.RawItem -> {
                    item(key = group.key) {
                        Box(modifier = Modifier.animateItem()) {
                            group.content()
                        }
                    }
                }

                is SettingsGraphGroup.Divider -> {
                    item(key = "divider_$groupIndex") {
                        HorizontalDivider(
                            modifier = Modifier
                                .animateItem()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun LazyItemScope.SettingsItemEntry(
    nodeKey: Any,
    title: String,
    description: String,
    iconVector: ImageVector?,
    @DrawableRes iconResId: Int?,
    shape: CustomCardShape,
    isHighlighted: Boolean,
    experimentalFlagText: String,
    behavior: ItemBehavior,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    isChecked: Boolean,
    selectedValue: Int,
    itemPaddingHorizontal: Dp,
    itemPaddingVertical: Dp,
    onClick: (Any) -> Unit,
    onToggle: (Any) -> Unit,
    onIntChanged: (Any, Int) -> Unit,
) {
    val onClickLambda = remember(nodeKey) { { onClick(nodeKey) } }
    val onToggleLambda = remember(nodeKey) { { onToggle(nodeKey) } }
    val onValueChangeLambda = remember(nodeKey) { { v: Int -> onIntChanged(nodeKey, v) } }

    SettingsItemView(
        modifier = Modifier
            .animateItem()
            .padding(
                horizontal = itemPaddingHorizontal,
                vertical = itemPaddingVertical
            ),
        title = title,
        description = description,
        icon = iconVector,
        iconResId = iconResId,
        shape = shape,
        isHighlighted = isHighlighted,
        experimentalFlagText = experimentalFlagText,
        behavior = behavior,
        enabled = enabled,
        hapticsEnabled = hapticsEnabled,
        isChecked = isChecked,
        selectedValue = selectedValue,
        onClick = onClickLambda,
        onToggle = onToggleLambda,
        onValueChange = onValueChangeLambda,
    )
}

