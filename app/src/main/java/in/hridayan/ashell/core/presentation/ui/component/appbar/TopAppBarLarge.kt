package `in`.hridayan.ashell.core.presentation.ui.component.appbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.lerp
import `in`.hridayan.ashell.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarLarge(
    modifier: Modifier = Modifier,
    title: String,
    scrollContent: @Composable (innerPadding: PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val collapsedFraction by remember { derivedStateOf { scrollBehavior.state.collapsedFraction } }
    val animatedFraction by animateFloatAsState(
        targetValue = collapsedFraction.coerceAtMost(0.99f),
        label = "animatedFraction"
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    val startStyle = MaterialTheme.typography.headlineLarge
                    val endStyle = MaterialTheme.typography.headlineSmall

                    val interpolatedTextStyle = if (collapsedFraction < 1f) {
                        TextStyle(
                            fontSize = lerp(startStyle.fontSize, endStyle.fontSize, animatedFraction)
                        )
                    } else {
                        endStyle
                    }

                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = interpolatedTextStyle
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {},
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding -> scrollContent(innerPadding) }
}
