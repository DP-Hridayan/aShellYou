package `in`.hridayan.ashell.settings.lookandfeel.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.ui.component.appbar.TopAppBarLarge
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookAndFeel() {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.Companion.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBarLarge(
                title = stringResource(id = R.string.look_and_feel),
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()

                ) {
                    Image(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.undrawImgPadding,
                                vertical = Dimens.paddingExtraLarge
                            ),
                        painter = painterResource(id = R.drawable.ic_undraw_theme),
                        contentDescription = null
                    )
                }
            }

        }
    }
}
