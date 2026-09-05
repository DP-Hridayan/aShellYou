package `in`.hridayan.settingsdsl.model

import androidx.compose.runtime.Immutable

/**
 * Describes how a settings item behaves when rendered and interacted with.
 */
sealed class ItemBehavior {

    /** Item is tapped to navigate or open a dialog. */
    data object Clickable : ItemBehavior()

    /** Item has a toggle switch. */
    data object Switch : ItemBehavior()

    /** Item renders as a full-width switch banner. */
    data object SwitchBanner : ItemBehavior()

    /**
     * Item contains a list of mutually exclusive radio options.
     *
     * Marked @Immutable because [options] is always constructed once by the DSL
     * and never mutated after creation.
     */
    @Immutable
    data class RadioGroup(val options: List<RadioButtonOption>) : ItemBehavior()

    /**
     * Item contains a segmented button group.
     *
     * Marked @Immutable because [options] is always constructed once by the DSL
     * and never mutated after creation.
     */
    @Immutable
    data class ButtonGroup(val options: List<ButtonGroupOption>) : ItemBehavior()
}
