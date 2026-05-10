package `in`.hridayan.ashell.settings.presentation.page.translators.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.UrlUtils
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
                    CrowdinContributeCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 15.dp)
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

@Composable
private fun CrowdinContributeCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_translate),
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.want_to_contribute_too),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp))

                Text(
                    text = stringResource(R.string.crowdin_contribution_msg),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp))

                IconWithTextButton(
                    icon = painterResource(R.drawable.ic_crowdin),
                    text = stringResource(R.string.crowdin),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    onClick = withHaptic {
                        UrlUtils.openUrl(
                            context = context,
                            url = UrlConst.URL_CROWDIN_PROJECT
                        )
                    })
            }
        }
    }
}