package `in`.hridayan.ashell.settings.presentation.page.contributors.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.settings.domain.model.Translator
import `in`.hridayan.ashell.settings.domain.repository.ContributorsRepository
import javax.inject.Inject

@HiltViewModel
class ContributorsViewModel @Inject constructor(
    private val repository: ContributorsRepository
) : ViewModel() {

    var translators by mutableStateOf<List<Translator>>(emptyList())
        private set

    init {
        translators = repository.getTranslators()
    }
}