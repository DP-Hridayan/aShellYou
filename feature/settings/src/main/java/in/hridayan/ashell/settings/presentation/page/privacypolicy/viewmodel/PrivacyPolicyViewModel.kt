package `in`.hridayan.ashell.settings.presentation.page.privacypolicy.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.model.PolicyBlock
import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.util.parsePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _blocks = mutableStateOf<List<PolicyBlock>>(emptyList())
    val blocks: State<List<PolicyBlock>> = _blocks

    init {
        viewModelScope.launch {
            _blocks.value = withContext(Dispatchers.IO) {
                val text = application.resources
                    .openRawResource(R.raw.privacy_policy)
                    .bufferedReader()
                    .use { it.readText() }

                val parsedBlocks = parsePolicy(text).toMutableList()
                // Remove the main document title since the top app bar already shows it
                if (parsedBlocks.firstOrNull()
                        ?.let { it is PolicyBlock.Heading && it.level == 1 } == true
                ) {
                    parsedBlocks.removeAt(0)
                }
                parsedBlocks
            }
        }
    }
}