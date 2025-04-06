package `in`.hridayan.ashell.core.presentation.ui.component.appbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.navigation.NavControllerHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarLarge(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable () -> Unit = {},
) {
    LargeTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
    ),
        title = {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Companion.Ellipsis,
        )
    },
        navigationIcon = {
        val navController = NavControllerHolder.navController
        IconButton(onClick = {
            navController?.popBackStack()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    },
        actions = { actions },
        scrollBehavior = scrollBehavior
    )
}
