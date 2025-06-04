package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.settings.data.local.model.PreferenceItem

@Composable
fun PreferenceItemView(item: PreferenceItem, modifier: Modifier = Modifier) {
    when (item) {
        is PreferenceItem.BoolPreferenceItem -> BooleanPreferenceItemView(
            item = item,
            modifier = modifier
        )

        is PreferenceItem.IntPreferenceItem -> IntPreferenceItemView(
            item = item,
            modifier = modifier
        )

        //    is PreferenceItem.StringPreferenceItem -> StringPreferenceItemView(item)
        //   is PreferenceItem.FloatPreferenceItem -> FloatPreferenceItemView(item)

        is PreferenceItem.NullPreferenceItem -> NullPreferenceItemView(
            item = item,
            modifier = modifier
        )

        else -> {}
    }
}