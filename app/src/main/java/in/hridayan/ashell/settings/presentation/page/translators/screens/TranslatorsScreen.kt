package `in`.hridayan.ashell.settings.presentation.page.translators.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.settings.domain.model.Translator
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.translators.viewmodel.ContributorsViewModel

@Composable
fun TranslatorsScreen(
    modifier: Modifier = Modifier,
    contributorsViewModel: ContributorsViewModel = hiltViewModel()
) {
    val translators = contributorsViewModel.translators.sortedBy { it.name }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.translators),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding
            ) {

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    )
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.crowdin),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .animateItem()
                    )
                }

                itemsIndexed(translators) { index, translator ->

                    val shape =
                        CardCornerShape.getRoundedShape(index = index, size = translators.size)

                    TranslatorCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        translator = translator,
                        shape = shape
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        })
}

@Composable
private fun TranslatorCard(
    modifier: Modifier = Modifier,
    translator: Translator,
    shape: CustomCardShape = CardCornerShape.SINGLE_CARD
) {
    CustomCard(
        modifier = modifier,
        shape = shape
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 10.dp)
                .basicMarquee(),
            text = translator.name,
            style = MaterialTheme.typography.titleMediumEmphasized
        )
        if (translator.languages.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, bottom = 10.dp)
                    .alpha(0.7f),
                text = translator.languages.joinToString(separator = ", "),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}