package `in`.hridayan.ashell.mirror.presentation.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.presentation.viewmodel.MirrorViewModel

@Composable
fun MirrorScreen(viewModel: MirrorViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val activity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onStart() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onStop(isChangingConfigurations = activity?.isChangingConfigurations == true)
    }

    KeepScreenOn()
    FollowVideoOrientation((uiState.session as? MirrorState.Streaming)?.videoSize)

    MirrorContent(
        uiState = uiState,
        actions = viewModel,
        onLeave = { navController.navigateBack() }
    )
}
