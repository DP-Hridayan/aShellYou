package `in`.hridayan.ashell.ui.navigation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.logcat.data.session.LogcatSessionHolder
import javax.inject.Inject

/** Thin HiltViewModel used solely to expose [LogcatSessionHolder] to NavGraph composables. */
@Stable
@HiltViewModel
class NavDeepLinkViewModel @Inject constructor(
    val sessionHolder: LogcatSessionHolder,
) : ViewModel()