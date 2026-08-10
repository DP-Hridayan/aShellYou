@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R

/**
 * Logcat screen top app bar.
 *
 * Play/Pause controls the logging service (start/stop).
 * Mode button (tab0 only) opens the source selection sheet.
 * Filter and Clear are secondary actions.
 */
@Composable
fun LogcatTopBar(
    isRunning: Boolean,
    showModeAction: Boolean,
    searchVisible: Boolean,
    isPreflightChecking: Boolean,
    onSearchToggle: () -> Unit,
    onPlayPause: () -> Unit,
    onModeClick: () -> Unit,
    onOpenFilter: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {
            Text(
                text = stringResource(R.string.logcat),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            // Search toggle
            IconButton(onClick = onSearchToggle) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.search),
                    tint = if (searchVisible) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            if (showModeAction) {
                IconButton(onClick = onModeClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.logcat_source),
                    )
                }
            }

            IconButton(
                onClick = onPlayPause,
                enabled = !isPreflightChecking,
            ) {
                if (isPreflightChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            if (isRunning) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isRunning) {
                            stringResource(R.string.stop)
                        } else {
                            stringResource(R.string.resume)
                        },
                        tint = if (isRunning) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }

            IconButton(onClick = onOpenFilter) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter_alt),
                    contentDescription = stringResource(R.string.filter),
                )
            }

            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.clear),
                )
            }
        }
    )
}
