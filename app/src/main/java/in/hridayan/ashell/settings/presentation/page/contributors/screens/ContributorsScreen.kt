package `in`.hridayan.ashell.settings.presentation.page.contributors.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.settings.domain.model.GitHubContributor
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.contributors.viewmodel.ContributorsViewModel

@Composable
fun ContributorsScreen(
    modifier: Modifier = Modifier,
    contributorsViewModel: ContributorsViewModel = hiltViewModel()
) {
    val contributors = contributorsViewModel.gitHubContributors
    val listState = rememberLazyListState()
    val context = LocalContext.current

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.contributors),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding
            ) {
                item {
                    AutoResizeableText(
                        text = stringResource(R.string.github),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                            .animateItem()
                    )
                }

                itemsIndexed(contributors) { index, contributor ->

                    val shape =
                        CardCornerShape.getRoundedShape(index = index, size = contributors.size)

                    GitHubContributorCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        contributor = contributor,
                        shape = shape,
                        onClick = {
                            UrlUtils.openUrl(
                                url = "https://github.com/${contributor.username}",
                                context = context
                            )
                        }
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
private fun GitHubContributorCard(
    modifier: Modifier = Modifier,
    contributor: GitHubContributor,
    shape: CustomCardShape = CardCornerShape.SINGLE_CARD,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier,
        shape = shape,
        onClick = withHaptic { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile picture with initials fallback
            ContributorAvatar(
                contributor = contributor,
                context = context
            )

            // Name and username
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(),
                    text = contributor.name,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f),
                    text = "@${contributor.username}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ContributorAvatar(
    contributor: GitHubContributor,
    context: Context,
    modifier: Modifier = Modifier
) {
    val initial = remember(contributor.name) {
        contributor.name.firstOrNull()?.uppercase() ?: "?"
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/${contributor.avatarAssetPath}")
            .build(),
        contentDescription = contributor.name,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        loading = {
            InitialsAvatar(initial = initial)
        },
        error = {
            InitialsAvatar(initial = initial)
        }
    )
}

@Composable
private fun InitialsAvatar(
    initial: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}