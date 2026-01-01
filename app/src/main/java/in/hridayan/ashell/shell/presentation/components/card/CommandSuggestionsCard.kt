@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.card

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel

@Composable
fun CommandSuggestionsCard(
    modifier: Modifier = Modifier,
    command: String,
    roundedCornerShape: RoundedCornerShape,
    viewModel: ShellViewModel = hiltViewModel()
) {
    RoundedCornerCard(
        modifier = modifier,
        onClick = withHaptic {
            viewModel.onCommandTextFieldChange(
                TextFieldValue(
                    text = command,
                )
            )
            viewModel.updateTextFieldSelection()
        },
        roundedCornerShape = roundedCornerShape,
    ) {
        Text(
            text = command,
            style = MaterialTheme.typography.titleMediumEmphasized,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 15.dp
            )
        )
    }
}