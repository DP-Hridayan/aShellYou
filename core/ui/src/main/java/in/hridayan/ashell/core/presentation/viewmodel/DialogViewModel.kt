package `in`.hridayan.ashell.core.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import javax.inject.Inject

@Stable
@HiltViewModel
class DialogViewModel @Inject constructor() : ViewModel() {
    private val _activeDialog = mutableStateOf<DialogKey?>(null)
    val activeDialog: DialogKey? get() = _activeDialog.value

    fun show(dialog: DialogKey) {
        _activeDialog.value = dialog
    }

    fun dismiss() {
        _activeDialog.value = null
    }

    val isDialogActive: Boolean
        get() = _activeDialog.value != null
}
