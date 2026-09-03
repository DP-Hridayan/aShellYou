package `in`.hridayan.settingsdsl.resolver

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.State
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
import `in`.hridayan.settingsdsl.ui.item.settingsContent

/**
 * Builder scope for providing dynamic state overrides to a [SettingsPage].
 */
class ResolverScope {
    internal val titleOverrides = mutableMapOf<SettingsKey<*>, @Composable () -> String>()
    internal val descriptionOverrides = mutableMapOf<SettingsKey<*>, @Composable () -> String>()
    internal val iconOverrides = mutableMapOf<SettingsKey<*>, @Composable () -> ImageVector?>()
    internal val visibilityOverrides = mutableMapOf<SettingsKey<*>, @Composable () -> Boolean>()
    internal val enabledOverrides = mutableMapOf<SettingsKey<*>, @Composable () -> Boolean>()

    fun overrideTitle(key: SettingsKey<*>, block: @Composable () -> String) {
        titleOverrides[key] = block
    }

    fun overrideDescription(key: SettingsKey<*>, block: @Composable () -> String) {
        descriptionOverrides[key] = block
    }

    fun overrideIcon(key: SettingsKey<*>, block: @Composable () -> ImageVector?) {
        iconOverrides[key] = block
    }

    fun overrideVisibility(key: SettingsKey<*>, block: @Composable () -> Boolean) {
        visibilityOverrides[key] = block
    }

    fun overrideEnabled(key: SettingsKey<*>, block: @Composable () -> Boolean) {
        enabledOverrides[key] = block
    }
}

/**
 * Resolves all groups in this [SettingsPage] into display-ready [ResolvedGroup]s.
 *
 * Call this **once** at the top of your screen composable, before the [LazyColumn].
 * All dynamic overrides are applied here. Visibility filtering and card shape computation
 * happen automatically.
 *
 * This function is `@Composable` — it reads any [State] or
 * [CompositionLocal] you reference in your override lambdas.
 * Recomposition is triggered automatically when those states change.
 *
 * @param highlightedKey The key of the item to visually highlight (e.g. from search). Null if none.
 * @param block Builder block to provide dynamic overrides for specific items.
 *
 * @return List of [ResolvedGroup]s ready to pass to [settingsContent].
 */
@Composable
fun SettingsPage.resolveAll(
    highlightedKey: SettingsKey<*>? = null,
    block: ResolverScope.() -> Unit = {}
): List<ResolvedGroup> {
    val scope = ResolverScope().apply(block)
    return groups.map { group ->
        group.resolve(
            titleOverrides = scope.titleOverrides,
            descriptionOverrides = scope.descriptionOverrides,
            iconOverrides = scope.iconOverrides,
            visibilityOverrides = scope.visibilityOverrides,
            enabledOverrides = scope.enabledOverrides,
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
    enabledOverride: Boolean = true,
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
        enabledOverride = enabledOverride,
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
    enabledOverrides: Map<SettingsKey<*>, @Composable () -> Boolean>,
    highlightedKey: SettingsKey<*>?,
): ResolvedGroup {
    return when (this) {
        is GroupSpec.Group -> {
            val visibleSpecs = items.filter { spec ->
                visibilityOverrides[spec.key]?.invoke() ?: spec.isVisible
            }
            val resolvedGroupTitle =
                if (titleResId != null) stringResource(titleResId) else title.takeIf { it.isNotEmpty() }

            ResolvedGroup.ItemGroup(
                groupTitle = resolvedGroupTitle,
                items = visibleSpecs.mapIndexed { i, spec ->
                    spec.toSettingsItem(
                        shape = cardShapeForPosition(i, visibleSpecs.size),
                        enabledOverride = enabledOverrides[spec.key]?.invoke() ?: spec.enabled,
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
    enabledOverride: Boolean,
    highlightedKey: SettingsKey<*>?,
): SettingsItem {
    val resolvedTitle = titleOverride
        ?: titleResId?.let { stringResource(it) }
        ?: titleString

    val resolvedDescription = descriptionOverride
        ?: descriptionResId?.let { stringResource(it) }
        ?: descriptionString

    val resolvedIcon: ImageVector? = iconOverride ?: iconVector

    val resolvedExperimentalFlagText = experimentalFlagTextResId?.let { stringResource(it) }
        ?: experimentalFlagText

    val behavior: ItemBehavior = when (this) {
        is ItemSpec.SwitchSpec -> ItemBehavior.Switch
        is ItemSpec.SwitchBannerSpec -> ItemBehavior.SwitchBanner
        is ItemSpec.ClickableSpec -> ItemBehavior.Clickable
        is ItemSpec.RadioGroupSpec -> ItemBehavior.RadioGroup(options)
        is ItemSpec.ButtonGroupSpec -> ItemBehavior.ButtonGroup(options)
    }

    return SettingsItem(
        key = key,
        enabled = enabledOverride,
        title = resolvedTitle,
        description = resolvedDescription,
        icon = resolvedIcon,
        iconResId = iconResId,
        shape = shape,
        behavior = behavior,
        isHighlighted = key == highlightedKey,
        experimentalFlagText = resolvedExperimentalFlagText
    )
}

private fun GroupSpec.specsOrEmpty(): List<ItemSpec> = when (this) {
    is GroupSpec.Group -> items
    else -> emptyList()
}



