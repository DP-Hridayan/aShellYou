@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.qstiles.presentation.components.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.qstiles.data.model.TileIcon

@Composable
fun IconChooserDialog(
    onDismiss: () -> Unit,
    icons: List<TileIcon>,
    searchQuery: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onIconSelected: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    DialogContainer(onDismiss = onDismiss) {

        DialogTitle(
            text = stringResource(R.string.choose_icon),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )

        CustomSearchBar(
            modifier = Modifier
                .fillMaxWidth(),
            value = searchQuery,
            onValueChange = { onQueryChange(it) },
            hint = stringResource(R.string.search_icons_here),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
            trailingIcon = {
                if (searchQuery.text.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear),
                        contentDescription = "Clear text",
                        modifier = Modifier
                            .clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    onQueryChange(TextFieldValue(""))
                                    focusManager.clearFocus()
                                }
                            )
                    )
                }
            }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(204.dp)
                .verticalScroll(scrollState)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
                maxItemsInEachRow = 5
            ) {
                icons.forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable(
                                onClick = withHaptic {
                                    onIconSelected(icon.id)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(icon.resId),
                            contentDescription = null
                        )
                    }
                }
            }

            if (icons.isEmpty()) {
                NoSearchResultUi(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun NoSearchResultUi(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Image(
            imageVector = DynamicColorImageVectors.noSearchResult(),
            contentDescription = null,
        )

        AutoResizeableText(
            text = stringResource(R.string.no_search_results_found),
            style = MaterialTheme.typography.bodyMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )
    }
}