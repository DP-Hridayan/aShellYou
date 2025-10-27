package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.settings.presentation.model.PreferenceItem

@Composable
fun PreferenceItemView(
    item: PreferenceItem,
    modifier: Modifier = Modifier,
    roundedShape: RoundedCornerShape = RoundedCornerShape(16.dp)
) {
    when (item) {
        is PreferenceItem.BoolPreferenceItem -> BooleanPreferenceItemView(
            item = item,
            modifier = modifier,
            roundedShape = roundedShape
        )

        is PreferenceItem.IntPreferenceItem -> IntPreferenceItemView(
            item = item,
            modifier = modifier
        )

        //    is PreferenceItem.StringPreferenceItem -> StringPreferenceItemView(item)
        //   is PreferenceItem.FloatPreferenceItem -> FloatPreferenceItemView(item)

        is PreferenceItem.NullPreferenceItem -> NullPreferenceItemView(
            item = item,
            modifier = modifier,
            roundedShape = roundedShape
        )

        else -> {}
    }
}