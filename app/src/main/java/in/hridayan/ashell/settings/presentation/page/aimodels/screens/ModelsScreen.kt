@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.aimodels.screens

import android.os.StatFs
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.presentation.ui.modelmanager.ModelCard
import `in`.hridayan.ashell.ai.presentation.ui.modelmanager.StorageUsageBar
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold

@Composable
fun ModelsScreen(
    modifier: Modifier = Modifier,
    viewModel: AiModelManagerViewModel = hiltViewModel(),
) {
    val models by viewModel.models.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()

    val context = LocalContext.current
    val totalAvailableBytes = remember {
        try {
            val stat = StatFs(context.filesDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            0L
        }
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.models),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                // Storage usage bar
                item {
                    StorageUsageBar(
                        usedBytes = storageUsage,
                        totalAvailableBytes = totalAvailableBytes
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Model cards
                items(
                    items = models,
                    key = { it.model.id }
                ) { modelState ->
                    ModelCard(
                        state = modelState,
                        onDownload = { viewModel.downloadModel(modelState.model.id) },
                        onCancelDownload = { viewModel.cancelDownload(modelState.model.id) },
                        onDelete = { viewModel.deleteModel(modelState.model.id) },
                        onSelect = { viewModel.selectModel(modelState.model.id) },
                        onDismissError = { viewModel.dismissError(modelState.model.id) }
                    )
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
        },
    )
}
