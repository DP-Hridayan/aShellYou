@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.ui.focus.focusRequester

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.logcat.domain.model.LogFilter

@Composable
fun LogcatTopBar(
    isRunning: Boolean,
    isAutoScrolling: Boolean,
    searchVisible: Boolean,
    activeFilter: LogFilter,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTogglePlayPause: () -> Unit,
    onOpenFilter: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchVisible) {
        if (searchVisible) focusRequester.requestFocus()
    }

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {
            AnimatedContent(
                targetState = searchVisible,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "search_toggle"
            ) { showSearch ->
                if (showSearch) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = activeFilter.searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onToggleSearch() }),
                        decorationBox = { inner ->
                            if (activeFilter.searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.logcat_filter_search_hint),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.logcat),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        },
        actions = {
            // Search toggle
            IconButton(onClick = onToggleSearch) {
                Icon(
                    painter = painterResource(
                        if (searchVisible) R.drawable.ic_cancel else `in`.hridayan.ashell.core.ui.R.drawable.ic_search
                    ),
                    contentDescription = stringResource(R.string.search),
                )
            }

            // Play / Pause — controls the logcat service (start/stop logging)
            // Red tint when running = tap to stop; green tint when stopped = tap to start
            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    painter = painterResource(
                        if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = if (isRunning)
                        stringResource(R.string.stop)
                    else
                        stringResource(R.string.resume),
                    tint = if (isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                )
            }

            // Filter
            IconButton(onClick = onOpenFilter) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter_alt),
                    contentDescription = stringResource(R.string.filter),
                )
            }

            // Clear
            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.clear),
                )
            }
        }
    )
}
