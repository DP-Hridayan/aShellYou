package `in`.hridayan.ashell.core.presentation.components.appbar

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.presentation.components.button.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarLarge(
    topBarTitle: String,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable () -> Unit = {},
) {
    LargeTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
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
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.05.em
            )
        },
        navigationIcon = { BackButton() },
        actions = { actions },
        scrollBehavior = scrollBehavior
    )
}
