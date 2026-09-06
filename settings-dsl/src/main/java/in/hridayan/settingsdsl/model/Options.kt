package `in`.hridayan.settingsdsl.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * A radio button option within a radio group item.
 *
 * @param value The integer value stored when this option is selected.
 * @param labelResId String resource for the display label.
 */
data class RadioButtonOption(
    val value: Int,
    @StringRes val labelResId: Int,
)

/**
 * A segmented button option within a button group item.
 *
 * @param value The integer value stored when this option is selected.
 * @param labelResId String resource for the display label.
 * @param iconResId Optional drawable for the button icon.
 */
data class ButtonGroupOption(
    val value: Int,
    @StringRes val labelResId: Int,
    @DrawableRes val iconResId: Int? = null,
)
