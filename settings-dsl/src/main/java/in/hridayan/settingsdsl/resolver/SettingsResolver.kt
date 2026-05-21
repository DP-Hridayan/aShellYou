package `in`.hridayan.settingsdsl.resolver

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsdsl.model.GroupSpec
import `in`.hridayan.settingsdsl.model.ItemBehavior
import `in`.hridayan.settingsdsl.model.ItemSpec
import `in`.hridayan.settingsdsl.model.ResolvedGroup
import `in`.hridayan.settingsdsl.model.SettingsItem
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.settingsdsl.model.SettingsPage
import `in`.hridayan.settingsdsl.ui.card.CustomCardShape
import `in`.hridayan.settingsdsl.ui.card.cardShapeForPosition

/**
 * Resolves all groups in this [SettingsPage] into display-ready [ResolvedGroup]s.
 *
 * Call this **once** at the top of your screen composable, before the [LazyColumn].
 * All dynamic overrides are applied here. Visibility filtering and card shape computation
 * happen automatically.
 *
 * This function is `@Composable` — it reads any [androidx.compose.runtime.State] or
 * [androidx.compose.runtime.CompositionLocal] you reference in your override lambdas.
 * Recomposition is triggered automatically when those states change.
 *
 * @param titleOverrides Map from [SettingsKey<*>] to a `@Composable` lambda that returns a
 *   custom title string. Overrides the static title for that item.
 * @param descriptionOverrides Map from [SettingsKey<*>] to a `@Composable` lambda that returns
 *   a dynamic description string. Overrides the static description for that item.
 * @param iconOverrides Map from [SettingsKey<*>] to a `@Composable` lambda that returns a
 *   dynamic [ImageVector]. Overrides the static icon for that item.
 * @param visibilityOverrides Map from [SettingsKey<*>] to a `@Composable` lambda that returns
 *   whether the item is visible. Overrides the static [visible] value from the DSL.
 * @param highlightedKey The key of the item to visually highlight (e.g. from search). Null if none.
 *
 * @return List of [ResolvedGroup]s ready to pass to [settingsContent].
 */
@Composable
fun SettingsPage.resolveAll(
    titleOverrides: Map<SettingsKey<*>, @Composable () -> String> = emptyMap(),
    descriptionOverrides: Map<SettingsKey<*>, @Composable () -> String> = emptyMap(),
    iconOverrides: Map<SettingsKey<*>, @Composable () -> ImageVector?> = emptyMap(),
    visibilityOverrides: Map<SettingsKey<*>, @Composable () -> Boolean> = emptyMap(),
    highlightedKey: SettingsKey<*>? = null,
): List<ResolvedGroup> {
    return groups.map { group ->
        group.resolve(
            titleOverrides = titleOverrides,
            descriptionOverrides = descriptionOverrides,
            iconOverrides = iconOverrides,
            visibilityOverrides = visibilityOverrides,
            highlightedKey = highlightedKey,
        )
    }
}

/**
 * Resolves a single item from this [SettingsPage] by [key].
 *
 * Useful when you need to extract and display a specific setting's current
 * resolved title/description elsewhere in your UI (e.g. a summary row).
 *
 * @param key The [SettingsKey<*>] of the item to resolve.
 * @param titleOverride Optional override for the item's title.
 * @param descriptionOverride Optional override for the item's description.
 * @param iconOverride Optional override for the item's icon.
 * @return The resolved [SettingsItem], or null if the key is not found.
 */
@Composable
fun SettingsPage.resolveItem(
    key: SettingsKey<*>,
    titleOverride: String? = null,
    descriptionOverride: String? = null,
    iconOverride: ImageVector? = null,
): SettingsItem? {
    val spec = groups
        .flatMap { it.specsOrEmpty() }
        .firstOrNull { it.key == key }
        ?: return null

    return spec.toSettingsItem(
        shape = CustomCardShape(all = 24.dp),
        titleOverride = titleOverride,
        descriptionOverride = descriptionOverride,
        iconOverride = iconOverride,
        highlightedKey = null,
    )
}

@Composable
private fun GroupSpec.resolve(
    titleOverrides: Map<SettingsKey<*>, @Composable () -> String>,
    descriptionOverrides: Map<SettingsKey<*>, @Composable () -> String>,
    iconOverrides: Map<SettingsKey<*>, @Composable () -> ImageVector?>,
    visibilityOverrides: Map<SettingsKey<*>, @Composable () -> Boolean>,
    highlightedKey: SettingsKey<*>?,
): ResolvedGroup {
    return when (this) {
        is GroupSpec.Items -> {
            val visibleSpecs = items.filter { spec ->
                visibilityOverrides[spec.key]?.invoke() ?: spec.isVisible
            }
            ResolvedGroup.ItemGroup(
                categoryTitle = null,
                items = visibleSpecs.mapIndexed { i, spec ->
                    spec.toSettingsItem(
                        shape = cardShapeForPosition(i, visibleSpecs.size),
                        titleOverride = titleOverrides[spec.key]?.invoke(),
                        descriptionOverride = descriptionOverrides[spec.key]?.invoke(),
                        iconOverride = iconOverrides[spec.key]?.invoke(),
                        highlightedKey = highlightedKey,
                    )
                },
            )
        }

        is GroupSpec.Category -> {
            val visibleSpecs = items.filter { spec ->
                visibilityOverrides[spec.key]?.invoke() ?: spec.isVisible
            }
            ResolvedGroup.ItemGroup(
                categoryTitle = stringResource(titleResId),
                items = visibleSpecs.mapIndexed { i, spec ->
                    spec.toSettingsItem(
                        shape = cardShapeForPosition(i, visibleSpecs.size),
                        titleOverride = titleOverrides[spec.key]?.invoke(),
                        descriptionOverride = descriptionOverrides[spec.key]?.invoke(),
                        iconOverride = iconOverrides[spec.key]?.invoke(),
                        highlightedKey = highlightedKey,
                    )
                },
            )
        }

        is GroupSpec.Custom -> ResolvedGroup.Custom(slot)
        GroupSpec.Divider -> ResolvedGroup.Divider
    }
}

@Composable
private fun ItemSpec.toSettingsItem(
    shape: CustomCardShape,
    titleOverride: String?,
    descriptionOverride: String?,
    iconOverride: ImageVector?,
    highlightedKey: SettingsKey<*>?,
): SettingsItem {
    val resolvedTitle = titleOverride
        ?: titleResId?.let { stringResource(it) }
        ?: titleString

    val resolvedDescription = descriptionOverride
        ?: descriptionResId?.let { stringResource(it) }
        ?: descriptionString

    val resolvedIcon: ImageVector? = iconOverride ?: iconVector

    val behavior: ItemBehavior = when (this) {
        is ItemSpec.SwitchSpec -> ItemBehavior.Switch
        is ItemSpec.SwitchBannerSpec -> ItemBehavior.SwitchBanner
        is ItemSpec.ClickableSpec -> ItemBehavior.Clickable
        is ItemSpec.RadioGroupSpec -> ItemBehavior.RadioGroup(options)
        is ItemSpec.ButtonGroupSpec -> ItemBehavior.ButtonGroup(options)
    }

    return SettingsItem(
        key = key,
        title = resolvedTitle,
        description = resolvedDescription,
        icon = resolvedIcon,
        iconResId = iconResId,
        shape = shape,
        behavior = behavior,
        isHighlighted = key == highlightedKey,
    )
}

private fun GroupSpec.specsOrEmpty(): List<ItemSpec> = when (this) {
    is GroupSpec.Items -> items
    is GroupSpec.Category -> items
    else -> emptyList()
}
