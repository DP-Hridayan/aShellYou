package `in`.hridayan.ashell.ui


import `in`.hridayan.ashell.core.common.SettingsKeys

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import `in`.hridayan.ashell.core.common.SettingsState
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

class SettingsStateImpl internal constructor(private val vm: SettingsViewModel) : SettingsState {

    @Composable
    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(key: SettingsKeys<T>): T {
        return when (val default = key.defaultValue) {
            is Boolean -> {
                val flow = remember(key) {
                    vm.getBoolean(key as SettingsKeys<Boolean>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is Int -> {
                val flow = remember(key) {
                    vm.getInt(key as SettingsKeys<Int>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is Float -> {
                val flow = remember(key) {
                    vm.getFloat(key as SettingsKeys<Float>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is String -> {
                val flow = remember(key) {
                    vm.getString(key as SettingsKeys<String>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            else -> error("Unsupported settings key type: ${key.name}")
        } }

    @Suppress("UNCHECKED_CAST")
    override fun <T> set(key: SettingsKeys<T>, value: T) {
        when (key.defaultValue) {
            is Boolean -> vm.setBoolean(key as SettingsKeys<Boolean>, value as Boolean)
            is Int -> vm.setInt(key as SettingsKeys<Int>, value as Int)
            is Float -> vm.setFloat(key as SettingsKeys<Float>, value as Float)
            is String -> vm.setString(key as SettingsKeys<String>, value as String)
            else -> error("Unsupported settings key type: ${key.name}")
        }
    }
}
