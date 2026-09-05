package `in`.hridayan.settingsdsl.ui

import `in`.hridayan.settingsdsl.dsl.SettingsDslMarker

/**
 * Holds global default callbacks used by [in.hridayan.settingsdsl.ui.SettingsColumn] when no
 * per-item override is defined in the DSL block.
 *
 * Build instances via [OnClickDefaultsScope] or the [onClickDefaults] top-level function.
 *
 * @param onSwitchItem Global toggle handler for [in.hridayan.settingsdsl.model.ItemBehavior.Switch]
 *                     and [in.hridayan.settingsdsl.model.ItemBehavior.SwitchBanner] items.
 *                     Receives the item key. Null means no-op.
 * @param onIntChanged Optional global handler for
 *                     [in.hridayan.settingsdsl.model.ItemBehavior.RadioGroup] and
 *                     [in.hridayan.settingsdsl.model.ItemBehavior.ButtonGroup] items.
 *                     Receives the item key and the newly selected index. Null means no-op.
 * @param isChecked Global boolean state reader for switch items. Receives the item key and returns
 *                  whether the switch should appear checked. Null defaults to `false`.
 * @param selectedValue Global integer state reader for radio/button group items. Receives the item
 *                      key and returns the currently selected index. Null defaults to `-1`.
 */
class OnClickDefaults internal constructor(
    val isChecked: ((Any) -> Boolean)? = null,
    val selectedValue: ((Any) -> Int)? = null,
    val onSwitchItem: ((Any) -> Unit)? = null,
    val onIntChanged: ((Any, Int) -> Unit)? = null,
)

/**
 * DSL scope for building [OnClickDefaults].
 *
 * Use this inside [rememberSettingsDslState] to register global default callbacks.
 */
@SettingsDslMarker
class OnClickDefaultsScope internal constructor() {
    private var isChecked: ((Any) -> Boolean)? = null
    private var selectedValue: ((Any) -> Int)? = null
    private var onSwitchItem: ((Any) -> Unit)? = null
    private var onIntChanged: ((Any, Int) -> Unit)? = null

    /**
     * Registers a global toggle handler for all switch and switch-banner items.
     *
     * @param block Lambda receiving the item key.
     */
    fun onSwitchItem(block: (Any) -> Unit) {
        onSwitchItem = block
    }

    /**
     * Registers a global value-change handler for radio group and button group items.
     *
     * Per-item [onIntChanged][in.hridayan.settingsdsl.dsl.RadioGroupItemBuilder.onIntChanged]
     * overrides this global default.
     *
     * @param block Lambda receiving the item key and the newly selected index.
     */
    fun onIntChanged(block: (Any, Int) -> Unit) {
        onIntChanged = block
    }

    /**
     * Registers a global boolean state reader for switch items.
     *
     * Per-item [isChecked][in.hridayan.settingsdsl.dsl.SwitchItemBuilder.isChecked]
     * overrides this global default.
     *
     * @param block Lambda receiving the item key and returning whether the switch is checked.
     */
    fun isChecked(block: (Any) -> Boolean) {
        isChecked = block
    }

    /**
     * Registers a global integer state reader for radio group and button group items.
     *
     * Per-item [selectedValue][in.hridayan.settingsdsl.dsl.RadioGroupItemBuilder.selectedValue]
     * overrides this global default.
     *
     * @param block Lambda receiving the item key and returning the currently selected index.
     */
    fun selectedValue(block: (Any) -> Int) {
        selectedValue = block
    }

    internal fun build(): OnClickDefaults = OnClickDefaults(
        onSwitchItem = onSwitchItem,
        onIntChanged = onIntChanged,
        isChecked = isChecked,
        selectedValue = selectedValue,
    )
}

/**
 * Builds an [OnClickDefaults] instance using the [OnClickDefaultsScope] DSL.
 *
 * @param block Builder block for registering default callbacks.
 */
fun onClickDefaults(block: OnClickDefaultsScope.() -> Unit): OnClickDefaults =
    OnClickDefaultsScope().apply(block).build()

