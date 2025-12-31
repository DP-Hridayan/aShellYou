package `in`.hridayan.ashell.core.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.presentation.utils.DialogKey
import javax.inject.Inject

@HiltViewModel
class DialogViewModel @Inject constructor() : ViewModel() {
    private val _activeDialog = mutableStateOf<DialogKey>(DialogKey.None)
    val activeDialog by _activeDialog

    fun show(dialog: DialogKey) {
        _activeDialog.value = dialog
    }

    fun dismiss() {
        _activeDialog.value = DialogKey.None
    }

    val isDialogActive: Boolean
        get() = _activeDialog.value != DialogKey.None
}
