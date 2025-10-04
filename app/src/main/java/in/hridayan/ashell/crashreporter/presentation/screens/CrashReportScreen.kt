package `in`.hridayan.ashell.crashreporter.presentation.screens

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.crashreporter.presentation.viewmodel.CrashViewModel

@Composable
fun CrashReportScreen(
    modifier: Modifier = Modifier,
    crashViewModel: CrashViewModel = hiltViewModel()
) {
    val latestCrash by crashViewModel.latestCrash

    Surface {
        Text("${latestCrash?.timestamp} \n${latestCrash?.stackTrace}")
    }
}