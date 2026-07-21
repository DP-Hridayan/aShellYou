package `in`.hridayan.ashell.settings.presentation.state

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.settingsdsl.model.SettingsKey
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.model.CustomSlot
import `in`.hridayan.settingsdsl.model.ResolvedGroup
import `in`.hridayan.settingsdsl.ui.item.settingsContent

import androidx.datastore.preferences.core.Preferences

/**
 * App-specific convenience wrapper around the library's [settingsContent].
 */
@Suppress("UNCHECKED_CAST")
fun LazyListScope.settingsContent(
    groups: List<ResolvedGroup>,
    viewModel: SettingsViewModel,
    prefs: Preferences,
    modifier: Modifier = Modifier,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    hapticsEnabled: Boolean = true,
    onItemClick: ((SettingsKey<*>) -> Unit)? = null,
    onBooleanToggle: ((SettingsKey<*>) -> Unit)? = null,
    onIntChanged: ((SettingsKey<*>, Int) -> Unit)? = null,
    customSlotContent: @Composable (CustomSlot) -> Unit = {},
    categoryHeader: (@Composable (String) -> Unit)? = null,
) {
    val vm = viewModel

    settingsContent(
        groups = groups,
        modifier = modifier,
        itemPaddingHorizontal = itemPaddingHorizontal,
        itemPaddingVertical = itemPaddingVertical,
        hapticsEnabled = hapticsEnabled,
        isChecked = { key ->
            val sk = key as? SettingsKeys<*> ?: return@settingsContent false
            if (sk.default !is Boolean) return@settingsContent false
            prefs[booleanPreferencesKey(sk.name)] ?: (sk.default as Boolean)
        },
        selectedValue = { key ->
            val sk = key as? SettingsKeys<*> ?: return@settingsContent -1
            if (sk.default !is Int) return@settingsContent -1
            prefs[intPreferencesKey(sk.name)] ?: (sk.default as Int)
        },
        onItemClick = onItemClick ?: { key: SettingsKey<*> -> vm.onItemClicked(key as SettingsKeys<*>) },
        onBooleanToggle = onBooleanToggle ?: { key: SettingsKey<*> -> vm.onToggle(key as SettingsKeys<Boolean>) },
        onIntChanged = onIntChanged ?: { key: SettingsKey<*>, v: Int -> vm.setInt(key as SettingsKeys<Int>, v) },
        customSlotContent = customSlotContent,
        categoryHeader = categoryHeader,
    )
}


