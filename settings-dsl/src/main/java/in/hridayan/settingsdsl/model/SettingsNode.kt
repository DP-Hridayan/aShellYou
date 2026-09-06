package `in`.hridayan.settingsdsl.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.settingsdsl.search.searchGraph
import `in`.hridayan.settingsdsl.ui.OnClickDefaults
import `in`.hridayan.settingsdsl.ui.SettingsColumn

/**
 * Internal runtime representation of a single settings item inside a [SettingsGraph].
 *
 * Produced by builder functions in `SettingsDslBuilders.kt` and consumed
 * by [SettingsColumn]. The search index is declared separately, via
 * [searchGraph].
 *
 * @param key The developer-supplied key identifying this setting. Can be any type.
 * @param keyName A string representation of [key] used for equality checks. Defaults to [key].toString().
 * @param isVisible Pure-Kotlin lambda evaluated to determine whether this item is rendered.
 *                  Must never call Compose APIs.
 * @param dynamicTitle A [Composable] lambda that returns the display title. When non-null, takes
 *                     precedence over [staticTitleRes] and [staticTitleString].
 * @param staticTitleRes String resource ID used when [dynamicTitle] is null.
 * @param staticTitleString Plain string used when [dynamicTitle] and [staticTitleRes] are null.
 * @param dynamicDescription A [Composable] lambda that returns the display description.
 * @param staticDescRes String resource ID for the description.
 * @param staticDescString Plain string description.
 * @param iconResId Drawable resource ID for the leading icon.
 * @param iconVector [ImageVector] for the leading icon.
 * @param experimentalFlagTextRes String resource for the experimental badge text. Null means no badge.
 * @param experimentalFlagTextString Plain string for the experimental badge text.
 * @param behavior The interaction type of this item.
 * @param radioOptions Options for [ItemBehavior.RadioGroup] items.
 * @param buttonOptions Options for [ItemBehavior.ButtonGroup] items.
 * @param onClickOverride Per-item click handler for [ItemBehavior.Clickable] items. When non-null,
 *                        takes precedence. There is no global default for clickable items.
 * @param onToggleOverride Per-item toggle handler for [ItemBehavior.Switch] and
 *                         [ItemBehavior.SwitchBanner] items. Overrides the global
 *                         [OnClickDefaults.onSwitchItem].
 * @param onIntChangedOverride Per-item value-change handler for [ItemBehavior.RadioGroup] and
 *                             [ItemBehavior.ButtonGroup] items. Overrides the global
 *                             [OnClickDefaults.onIntChanged].
 * @param isCheckedOverride Per-item boolean state reader for switch items. Overrides the global
 *                          [OnClickDefaults.isChecked].
 * @param selectedValueOverride Per-item integer state reader for radio/button group items. Overrides
 *                              the global [OnClickDefaults.selectedValue].
 */
internal data class SettingsNode(
    val key: Any,
    val keyName: String,
    val isVisible: () -> Boolean,
    val dynamicTitle: (@Composable () -> String)?,
    @StringRes val staticTitleRes: Int?,
    val staticTitleString: String,
    val dynamicDescription: (@Composable () -> String)?,
    @StringRes val staticDescRes: Int?,
    val staticDescString: String,
    @DrawableRes val iconResId: Int?,
    val iconVector: ImageVector?,
    @StringRes val experimentalFlagTextRes: Int?,
    val experimentalFlagTextString: String,
    val enabled: Boolean,
    val behavior: ItemBehavior,
    val radioOptions: List<RadioButtonOption> = emptyList(),
    val buttonOptions: List<ButtonGroupOption> = emptyList(),
    val onClickOverride: ((Any) -> Unit)? = null,
    val onToggleOverride: ((Any) -> Unit)? = null,
    val onIntChangedOverride: ((Any, Int) -> Unit)? = null,
    val isCheckedOverride: ((Any) -> Boolean)? = null,
    val selectedValueOverride: ((Any) -> Int)? = null,
)
