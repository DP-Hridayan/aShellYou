@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.components.scaffold

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.navigation.navigateBack

/**
 * @param topAppBarState Optional external [TopAppBarState] created by the caller.
 *   Pass the same instance to [rememberHighlightState] so the search-highlight scroll
 *   can collapse the top bar before animating to the target item.
 *   If null, an internal state is created (normal usage with no highlight scroll).
 */
@Composable
fun SettingsScaffold(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    listState: LazyListState,
    topAppBarState: TopAppBarState? = null,
    content: @Composable (innerPadding: PaddingValues, topBarScrollBehavior: TopAppBarScrollBehavior) -> Unit,
    fabContent: @Composable (expanded: Boolean) -> Unit = {}
) {
    val expanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset < 10
        }
    }

    SettingsScaffoldImpl(
        modifier = modifier,
        topBarTitle = topBarTitle,
        expanded = expanded,
        topAppBarState = topAppBarState,
        content = content,
        fabContent = fabContent
    )
}

@Composable
fun SettingsScaffold(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    scrollState: ScrollState,
    topAppBarState: TopAppBarState? = null,
    content: @Composable (innerPadding: PaddingValues, topBarScrollBehavior: TopAppBarScrollBehavior) -> Unit,
    fabContent: @Composable (expanded: Boolean) -> Unit = {}
) {
    val expanded by remember {
        derivedStateOf {
            scrollState.value < 10
        }
    }

    SettingsScaffoldImpl(
        modifier = modifier,
        topBarTitle = topBarTitle,
        expanded = expanded,
        topAppBarState = topAppBarState,
        content = content,
        fabContent = fabContent
    )
}

@Composable
private fun SettingsScaffoldImpl(
    modifier: Modifier = Modifier,
    topBarTitle: String,
    expanded: Boolean,
    topAppBarState: TopAppBarState? = null,
    content: @Composable (innerPadding: PaddingValues, topBarScrollBehavior: TopAppBarScrollBehavior) -> Unit,
    fabContent: @Composable (expanded: Boolean) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        topAppBarState ?: rememberTopAppBarState()
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val expandedFontSize = 33.sp
                    val collapsedFontSize = 20.sp

                    val fontSize = lerp(expandedFontSize, collapsedFontSize, collapsedFraction)
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = topBarTitle,
                        maxLines = 1,
                        fontSize = fontSize,
                        letterSpacing = 0.05.em
                    )
                },
                navigationIcon = {
                    val navController = `in`.hridayan.ashell.navigation.LocalNavController.current
                    BackButton(onClick = { navController.navigateBack() })
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        },
        floatingActionButton = {
            fabContent(expanded)
        }) { innerPadding ->

        // Pass the SAME scrollBehavior used in LargeTopAppBar to the content
        content(innerPadding, scrollBehavior)
    }
}